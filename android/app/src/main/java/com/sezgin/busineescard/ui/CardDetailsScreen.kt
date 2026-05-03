package com.sezgin.busineescard.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.sezgin.busineescard.services.GoogleWalletService
import com.sezgin.busineescard.utils.QRCodeGenerator
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(userId: String, cardId: String, onBack: () -> Unit, onOpenMarket: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val dbService = remember { DatabaseService(context) }
    var card by remember { mutableStateOf(dbService.getCardById(userId, cardId)) }
    var showShareOptions by remember { mutableStateOf(false) }
    var canAddToGoogleWallet by remember { mutableStateOf(false) }

    val nfcAdapter = remember(context) { NfcAdapter.getDefaultAdapter(context) }
    val isNfcSupported = nfcAdapter != null
    var isNfcEnabled by remember(nfcAdapter) { mutableStateOf(nfcAdapter?.isEnabled == true) }

    val nfcStatusText = if (isNfcEnabled) "NFC Aktif" else "NFC Kapali"
    val nfcStatusColor = if (isNfcEnabled) Color(0xFF2E7D32) else Color(0xFFC62828)
    val nfcStatusBackground = if (isNfcEnabled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    // Re-fetch card data when returning from market
    LaunchedEffect(Unit) {
        card = dbService.getCardById(userId, cardId)
    }

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
    val walletLabel = if (canAddToGoogleWallet) "Google Wallet'a ekle" else "Google Wallet (hazirlaniyor)"

    DisposableEffect(activity) {
        if (activity != null) {
            GoogleWalletService.checkAvailability(activity) { available ->
                canAddToGoogleWallet = available
            }
        } else {
            canAddToGoogleWallet = false
        }
        onDispose { }
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
                        card?.let {
                            if (activity != null && canAddToGoogleWallet) {
                                GoogleWalletService.saveBusinessCardPass(activity, it)
                            } else {
                                addToGoogleWallet(context, it)
                            }
                        }
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
                        Text(walletLabel, fontWeight = FontWeight.Medium)
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
                    IconButton(onClick = onOpenMarket) {
                        Icon(Icons.Default.ShoppingCart, null)
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

                StandardBusinessCard(
                    name = render.name,
                    title = render.title,
                    company = render.company,
                    address = render.address,
                    email = render.email,
                    phone1 = render.phones,
                    phone2 = render.phones2,
                    templateId = render.templateId,
                    fontStyle = render.fontStyle,
                    qrBitmap = qrBitmap
                )

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
                    "Paylas butonundan resmi paylasabilirsiniz. Wallet ayarlari tamamlandiginda ayni ekrandan Google Wallet'a da ekleyebilirsiniz.",
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
    val width = CardStyle.CANVAS_WIDTH.toInt()
    val height = CardStyle.CANVAS_HEIGHT.toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgResId = backgroundResId(card.templateId)
    val bgBitmap = BitmapFactory.decodeResource(context.resources, bgResId)
    canvas.drawBitmap(Bitmap.createScaledBitmap(bgBitmap, width, height, true), 0f, 0f, null)
    
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
    }
    
    val typeface = android.graphics.Typeface.create(
        when (card.fontStyle) {
            "SansSerif" -> android.graphics.Typeface.SANS_SERIF
            "Serif" -> android.graphics.Typeface.SERIF
            "Monospace" -> android.graphics.Typeface.MONOSPACE
            else -> android.graphics.Typeface.DEFAULT
        },
        android.graphics.Typeface.NORMAL
    )
    val typefaceBold = android.graphics.Typeface.create(typeface, android.graphics.Typeface.BOLD)

    textPaint.color = CardStyle.canvasTextColor(card.templateId)
    
    // İsim
    textPaint.textSize = CardStyle.CANVAS_NAME_SIZE
    textPaint.typeface = typefaceBold
    canvas.drawText(card.name.uppercase(), CardStyle.DEFAULT_NAME_X, CardStyle.DEFAULT_NAME_Y + CardStyle.CANVAS_NAME_SIZE, textPaint)
    
    // Ünvan
    textPaint.textSize = CardStyle.CANVAS_TITLE_SIZE
    textPaint.typeface = typeface
    canvas.drawText(card.title, CardStyle.DEFAULT_TITLE_X, CardStyle.DEFAULT_TITLE_Y + CardStyle.CANVAS_TITLE_SIZE, textPaint)
    
    // Şirket
    textPaint.textSize = CardStyle.CANVAS_COMPANY_SIZE
    canvas.drawText(card.company, CardStyle.DEFAULT_COMPANY_X, CardStyle.DEFAULT_COMPANY_Y + CardStyle.CANVAS_COMPANY_SIZE, textPaint)
    
    // Detaylar
    val detailsX = CardStyle.DEFAULT_DETAILS_X
    var currentY = CardStyle.DEFAULT_DETAILS_Y
    textPaint.textSize = CardStyle.CANVAS_ADDRESS_SIZE
    
    card.address.split("\n").forEach { line ->
        canvas.drawText(line, detailsX, currentY + CardStyle.CANVAS_ADDRESS_SIZE, textPaint)
        currentY += CardStyle.CANVAS_ADDRESS_LINE_HEIGHT
    }
    currentY += CardStyle.DETAILS_LINE_GAP
    textPaint.textSize = CardStyle.CANVAS_CONTACT_SIZE
    if (card.email.isNotBlank()) {
        canvas.drawText(card.email, detailsX, currentY + CardStyle.CANVAS_CONTACT_SIZE, textPaint)
        currentY += CardStyle.CANVAS_CONTACT_SIZE + CardStyle.EMAIL_TO_PHONE_GAP
    }
    if (card.phones.isNotBlank()) {
        canvas.drawText(card.phones, detailsX, currentY + CardStyle.CANVAS_CONTACT_SIZE, textPaint)
        currentY += CardStyle.CANVAS_CONTACT_SIZE + CardStyle.PHONE_LINE_GAP
    }
    card.phones2?.takeIf { it.isNotBlank() }?.let {
        canvas.drawText(it, detailsX, currentY + CardStyle.CANVAS_CONTACT_SIZE, textPaint)
    }

    createQrBitmap(card.qrContent)?.let {
        val qrSize = CardStyle.QR_FRAME_SIZE_CANVAS
        val bitSize = CardStyle.QR_BIT_SIZE_CANVAS
        val qrLeft = CardStyle.DEFAULT_QR_X
        val qrTop = CardStyle.DEFAULT_QR_Y
        val offset = (qrSize - bitSize) / 2
        
        val qrRect = android.graphics.RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
        val whitePaint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRoundRect(qrRect, CardStyle.QR_CORNER_RADIUS_CANVAS, CardStyle.QR_CORNER_RADIUS_CANVAS, whitePaint)
        canvas.drawBitmap(Bitmap.createScaledBitmap(it, bitSize.toInt(), bitSize.toInt(), false), qrLeft + offset, qrTop + offset, null)
    }
    
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/card.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(cachePath, "card.jpg"))
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/jpeg"
        }, "Paylas"))
    } catch (e: Exception) { e.printStackTrace() }
}

private fun addToGoogleWallet(context: Context, card: BusinessCard) {
    Toast.makeText(context, "Google Wallet hazir degil.", Toast.LENGTH_LONG).show()
}

private fun createQrBitmap(qrContent: String): Bitmap? {
    if (qrContent.isBlank()) return null
    return QRCodeGenerator.generateQRCode(qrContent, 512)
}

private data class CardPresentationData(
    val name: String, val title: String, val company: String, val address: String,
    val email: String, val phones: String, val phones2: String?,
    val templateId: Int, val fontStyle: String, val qrContent: String
) {
    companion object {
        fun from(card: BusinessCard): CardPresentationData {
            val (phone1Code, phone1Local) = splitPhoneNumber(card.phones)
            val phones1 = normalizePhoneWithCode(phone1Code, phone1Local)
            val phones2 = card.phones2?.takeIf { it.isNotBlank() }?.let {
                val (code, local) = splitPhoneNumber(it)
                normalizePhoneWithCode(code, local)
            }
            return CardPresentationData(
                name = card.name, title = card.title, company = card.company,
                address = card.address, email = card.email, phones = phones1, phones2 = phones2,
                templateId = card.templateId, fontStyle = card.fontStyle,
                qrContent = buildVCard(card, phones1, phones2)
            )
        }
    }
}

fun buildVCard(card: BusinessCard, phones1: String, phones2: String?): String {
    var vcard = "BEGIN:VCARD\nVERSION:3.0\nN:${card.name}\nORG:${card.company}\nTITLE:${card.title}\nTEL;TYPE=CELL:$phones1\n"
    if (!phones2.isNullOrEmpty()) vcard += "TEL;TYPE=WORK:$phones2\n"
    vcard += "EMAIL:${card.email}\nADR:${card.address}\nURL:${card.website}\nEND:VCARD"
    return vcard
}
