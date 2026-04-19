package com.sezgin.busineescard.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.nfc.NfcAdapter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
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
    val card = remember { dbService.getCardById(userId, cardId) }

    val nfcAdapter = try { NfcAdapter.getDefaultAdapter(context) } catch (e: Exception) { null }
    val isNfcEnabled = nfcAdapter?.isEnabled == true

    val qrContent = card?.let { "BEGIN:VCARD\nVERSION:3.0\nN:${it.name}\nORG:${it.company}\nTITLE:${it.title}\nTEL:${it.phones}\nEMAIL:${it.email}\nADR:${it.address}\nURL:${it.website}\nEND:VCARD" } ?: ""
    val qrBitmap = remember(qrContent) { if (qrContent.isNotEmpty()) QRCodeGenerator.generateQRCode(qrContent, 200) else null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kart Detayı", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { /* Edit logic */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        if (card == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Kart bulunamadı")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // NFC Durum Pill
                val nfcColor = if (isNfcEnabled) Color(0xFF2E7D32) else Color(0xFFC62828)
                val nfcText = if (isNfcEnabled) "NFC Paylaşım Aktif" else "NFC Kapalı"
                
                Surface(
                    shape = CircleShape,
                    color = nfcColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, nfcColor.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Refresh ikonu NFC ikonu yerine garantidir
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null, 
                            tint = nfcColor, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(nfcText, color = nfcColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Kart Önizleme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        Column(modifier = Modifier.align(Alignment.TopStart)) {
                            Text(card.name.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
                            Text(card.title, fontSize = 16.sp, color = Color.DarkGray)
                            Text(card.company, fontSize = 16.sp, color = Color.DarkGray)
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Text(card.address, fontSize = 12.sp, color = Color.Gray)
                            Text(card.email, fontSize = 12.sp, color = Color.Gray)
                            Text(card.phones, fontSize = 12.sp, color = Color.Gray)
                        }

                        if (qrBitmap != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .size(110.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Paylaş Butonu
                Button(
                    onClick = { shareCardAsImage(context, card, qrBitmap) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("KARTVİZİTİ PAYLAŞ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Paylaş butonuna bastığınızda kartınız yüksek kaliteli bir resim olarak gönderilir.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

private fun shareCardAsImage(context: Context, card: BusinessCard, qrBitmap: Bitmap?) {
    val width = 1000
    val height = 600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = android.graphics.Paint()
    paint.color = android.graphics.Color.parseColor("#E8F5E9")
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    
    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#2C3E50")
        textSize = 60f
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    
    canvas.drawText(card.name.uppercase(), 60f, 120f, textPaint)
    textPaint.textSize = 40f
    textPaint.typeface = android.graphics.Typeface.DEFAULT
    canvas.drawText(card.title, 60f, 180f, textPaint)
    canvas.drawText(card.company, 60f, 230f, textPaint)
    
    textPaint.textSize = 30f
    textPaint.color = android.graphics.Color.GRAY
    canvas.drawText(card.address, 60f, 450f, textPaint)
    canvas.drawText(card.email, 60f, 500f, textPaint)
    canvas.drawText(card.phones, 60f, 550f, textPaint)
    
    qrBitmap?.let {
        val scaledQr = Bitmap.createScaledBitmap(it, 220, 220, false)
        canvas.drawBitmap(scaledQr, (width - 280).toFloat(), (height - 280).toFloat(), null)
    }
    
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/business_card.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
        
        val newFile = File(cachePath, "business_card.jpg")
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
        
        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/jpeg"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Kartı Paylaş"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
