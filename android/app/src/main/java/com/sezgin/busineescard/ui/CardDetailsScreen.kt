package com.sezgin.busineescard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.DatabaseService
import com.sezgin.busineescard.utils.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(userId: String, cardId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    val card = remember { dbService.getCardById(userId, cardId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kart Görünümü") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kart Önizleme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor("#" + card.cardColor.removePrefix("0xFF")))
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val textColor = if (card.cardColor == "0xFF2C2C2C") Color.White else Color.Black
                            Text(card.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text(card.title, fontSize = 16.sp, color = textColor.copy(alpha = 0.8f))
                        }
                        Column {
                            val textColor = if (card.cardColor == "0xFF2C2C2C") Color.White else Color.Black
                            Text(card.company, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
                            Text(card.phones, fontSize = 12.sp, color = textColor)
                            Text(card.email, fontSize = 12.sp, color = textColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                Text("Karekod ile Paylaş", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                val qrContent = "BEGIN:VCARD\nVERSION:3.0\nN:${card.name}\nORG:${card.company}\nTITLE:${card.title}\nTEL:${card.phones}\nEMAIL:${card.email}\nADR:${card.address}\nURL:${card.website}\nEND:VCARD"
                val qrBitmap = remember { QRCodeGenerator.generateQRCode(qrContent) }

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedButton(
                    onClick = { /* Paylaşım mantığı eklenebilir */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kartı Paylaş")
                }
            }
        }
    }
}
