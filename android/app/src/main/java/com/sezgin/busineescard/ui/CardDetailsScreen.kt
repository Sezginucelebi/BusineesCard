package com.sezgin.busineescard.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.nfc.NfcAdapter
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sezgin.busineescard.R
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.DatabaseService
import com.sezgin.busineescard.utils.QRCodeGenerator
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(userId: String, cardId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    var card by remember { mutableStateOf(dbService.getCardById(userId, cardId)) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showShareOptions by remember { mutableStateOf(false) }

    val nfcAdapter = remember(context) { NfcAdapter.getDefaultAdapter(context) }
    val isNfcSupported = nfcAdapter != null
    var isNfcEnabled by remember(nfcAdapter) { mutableStateOf(nfcAdapter?.isEnabled == true) }

    val nfcStatusText = if (isNfcEnabled) "NFC Aktif" else "NFC Kapali"
    val nfcStatusColor = if (isNfcEnabled) Color(0xFF2E7D32) else Color(0xFFC62828)
    val nfcStatusBackground = if (isNfcEnabled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    DisposableEffect(context, nfcAdapter) {
        if (nfcAdapter == null) {
            onDispose { }
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                        isNfcEnabled = nfcAdapter.isEnabled
                    }
                }
            }
            isNfcEnabled = nfcAdapter.isEnabled
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose {
                runCatching { context.unregisterReceiver(receiver) }
            }
        }
    }

    val cardRenderData = remember(card) { card?.let { CardPresentationData.from(it) } }
    val qrBitmap = remember(cardRenderData?.qrContent) { cardRenderData?.qrContent?.let(::createQrBitmap) }

    val bgResId = when (cardRenderData?.templateId ?: 1) {
        1 -> R.drawable.card_bg_1
        2 -> R.drawable.card_bg_2
        3 -> R.drawable.card_bg_3
        else -> R.drawable.card_bg_1
    }

    if (showTemplatePicker) {
        ModalBottomSheet(onDismissRequest = { showTemplatePicker = false }) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Kart Tasarimi Secin", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    listOf(1, 2, 3).forEach { id ->
                        val thumb = when (id) {
                            1 -> R.drawable.card_bg_1
                            2 -> R.drawable.card_bg_2
                            else -> R.drawable.card_bg_3
                        }
                        Card(
                            onClick = {
                                card?.let {
                                    val updatedCard = it.copy(templateId = id)
                                    dbService.insertCard(updatedCard)
                                    card = updatedCard
                                }
                                showTemplatePicker = false
                            },
                            modifier = Modifier.size(80.dp),
                            border = BorderStroke(
                                2.dp,
                                if (card?.templateId == id) Color(0xFF2C3E50) else Color.Transparent
                            )
                        ) {
                            Image(painterResource(thumb), null, contentScale = ContentScale.Crop)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showShareOptions) {
        ModalBottomSheet(onDismissRequest = { showShareOptions = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Paylas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Card(
                    onClick = {
                        cardRenderData?.let { shareCardAsImage(context, it) }
                        showShareOptions = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2C3E50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Resim olarak paylas", fontWeight = FontWeight.Medium)
                    }
                }
                Card(
                    onClick = {
                        card?.let { addToGoogleWallet(context, it) }
                        showShareOptions = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_save),
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Google Wallet'a ekle", fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kart Detayi", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { showTemplatePicker = true }) {
                        Icon(Icons.Default.Settings, null)
                    }
                }
            )
        }
    ) { padding ->
        cardRenderData?.let { render ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = CircleShape,
                    color = nfcStatusBackground,
                    border = BorderStroke(1.dp, nfcStatusColor.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = android.R.drawable.stat_notify_sync),
                            contentDescription = null,
                            tint = nfcStatusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isNfcSupported) nfcStatusText else "NFC Desteklenmiyor",
                            color = nfcStatusColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1050f / 600f).shadow(
                        10.dp,
                        RoundedCornerShape(28.dp)
                    )
                ) {
                    Image(
                        painter = painterResource(bgResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Box(modifier = Modifier.fillMaxSize().padding(28.dp)) {
                        Column(modifier = Modifier.align(Alignment.TopStart)) {
                            Text(
                                render.name.uppercase(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (render.templateId == 2) Color.White else Color(0xFF2C3E50)
                            )
                            Text(
                                render.title,
                                fontSize = 16.sp,
                                color = if (render.templateId == 2) Color.LightGray else Color(
                                    0xFF2C3E50
                                ).copy(0.8f)
                            )
                            Text(
                                render.company,
                                fontSize = 16.sp,
                                color = if (render.templateId == 2) Color.LightGray else Color(
                                    0xFF2C3E50
                                ).copy(0.8f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                                Text(
                                    render.address,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = if (render.templateId == 2) Color.White.copy(0.7f) else Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    render.email,
                                    fontSize = 11.sp,
                                    color = if (render.templateId == 2) Color.White.copy(0.7f) else Color.DarkGray
                                )
                                Text(
                                    render.phones,
                                    fontSize = 11.sp,
                                    color = if (render.templateId == 2) Color.White.copy(0.7f) else Color.DarkGray
                                )
                                if (!render.phones2.isNullOrEmpty()) {
                                    Text(
                                        render.phones2,
                                        fontSize = 11.sp,
                                        color = if (render.templateId == 2) Color.White.copy(0.7f) else Color.DarkGray
                                    )
                                }
                            }
                        }
                        if (qrBitmap != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(99.dp).align(Alignment.BottomEnd).shadow(
                                    2.dp,
                                    RoundedCornerShape(12.dp)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.padding(6.dp).fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { showShareOptions = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp).shadow(
                        4.dp,
                        RoundedCornerShape(18.dp)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "KARTVIZITI PAYLAS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Paylas butonundan resmi paylasabilir veya Google Wallet akisini baslatabilirsiniz.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
            }
        }
    }
}

private fun shareCardAsImage(context: Context, card: CardPresentationData) {
    val width = 1050
    val height = 600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgResId = when (card.templateId) {
        1 -> R.drawable.card_bg_1
        2 -> R.drawable.card_bg_2
        3 -> R.drawable.card_bg_3
        else -> R.drawable.card_bg_1
    }
    val bgBitmap = BitmapFactory.decodeResource(context.resources, bgResId)
    canvas.drawBitmap(Bitmap.createScaledBitmap(bgBitmap, width, height, true), 0f, 0f, null)
    val textPaint = android.graphics.Paint().apply {
        color = if (card.templateId == 2) android.graphics.Color.WHITE else android.graphics.Color.parseColor(
            "#2C3E50"
        )
        isAntiAlias = true
    }
    textPaint.textSize = 65f
    textPaint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    canvas.drawText(card.name.uppercase(), 60f, 100f, textPaint)
    textPaint.textSize = 38f
    textPaint.typeface = android.graphics.Typeface.DEFAULT
    canvas.drawText(card.title, 60f, 160f, textPaint)
    canvas.drawText(card.company, 60f, 210f, textPaint)
    textPaint.textSize = 30f
    textPaint.color = if (card.templateId == 2) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
    canvas.drawText(card.address, 60f, 440f, textPaint)
    canvas.drawText(card.email, 60f, 490f, textPaint)
    canvas.drawText(card.phones, 60f, 540f, textPaint)
    card.phones2?.let { canvas.drawText(it, 60f, 580f, textPaint) }
    createQrBitmap(card.qrContent)?.let {
        val qrFrameSize = 297 // %10 küçültüldü (330 -> 297)
        val qrSize = 252      // %10 küçültüldü (280 -> 252)
        val qrLeft = width - qrFrameSize - 40
        val qrTop = height - qrFrameSize - 40
        val qrOffset = ((qrFrameSize - qrSize) / 2).toFloat()
        val qrRect = android.graphics.RectF(
            qrLeft.toFloat(),
            qrTop.toFloat(),
            (qrLeft + qrFrameSize).toFloat(),
            (qrTop + qrFrameSize).toFloat()
        )
        val whitePaint = android.graphics.Paint().apply { 
            color = android.graphics.Color.WHITE 
            setShadowLayer(10f, 0f, 5f, android.graphics.Color.argb(50, 0, 0, 0))
        }
        canvas.drawRoundRect(qrRect, 30f, 30f, whitePaint)
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(it, qrSize, qrSize, false),
            (qrLeft + qrOffset).toFloat(),
            (qrTop + qrOffset).toFloat(),
            null
        )
    }
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/card.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(cachePath, "card.jpg")
        )
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/jpeg"
        }, "Paylas"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun addToGoogleWallet(context: Context, card: BusinessCard) {
    val walletIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://pay.google.com/gp/v/save/")
    }
    if (walletIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(walletIntent)
    } else {
        Toast.makeText(
            context,
            "${card.name} icin Google Wallet entegrasyonu hazirlaniyor.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun createQrBitmap(qrContent: String): Bitmap? {
    if (qrContent.isBlank()) return null
    return QRCodeGenerator.generateQRCode(qrContent, 512)
}

private data class CardPresentationData(
    val name: String,
    val title: String,
    val company: String,
    val address: String,
    val email: String,
    val phones: String,
    val phones2: String?,
    val templateId: Int,
    val qrContent: String
) {
    companion object {
        fun from(card: BusinessCard): CardPresentationData {
            val phones1 = if (card.phones.startsWith("+")) card.phones else "+${card.phones}"
            val phones2 = card.phones2?.takeIf { it.isNotBlank() }?.let { if (it.startsWith("+")) it else "+$it" }
            val qrContent = buildVCard(card, phones1, phones2)
            return CardPresentationData(
                name = card.name,
                title = card.title,
                company = card.company,
                address = card.address,
                email = card.email,
                phones = phones1,
                phones2 = phones2,
                templateId = card.templateId,
                qrContent = qrContent
            )
        }
    }
}

private fun buildVCard(card: BusinessCard, phones1: String, phones2: String?): String {
    var vcard =
        "BEGIN:VCARD\nVERSION:3.0\nN:${card.name}\nORG:${card.company}\nTITLE:${card.title}\nTEL;TYPE=CELL:$phones1\n"
    if (!phones2.isNullOrEmpty()) vcard += "TEL;TYPE=WORK:$phones2\n"
    vcard += "EMAIL:${card.email}\nADR:${card.address}\nURL:${card.website}\nEND:VCARD"
    return vcard
}
