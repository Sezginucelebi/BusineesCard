package com.sezgin.busineescard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sezgin.busineescard.R
import com.sezgin.busineescard.services.AuthService
import com.sezgin.busineescard.services.DatabaseService
import com.sezgin.busineescard.services.SubscriptionLevel
import com.sezgin.busineescard.utils.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(userId: String, cardId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    val authService = remember { AuthService(context) }
    var currentCard by remember { mutableStateOf(if (cardId != null) dbService.getCardById(userId, cardId) else null) }
    val subLevel = authService.getSubscriptionLevel()

    val previewQrBitmap = remember(currentCard) { QRCodeGenerator.generateQRCode("PREVIEW", 512) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasarım & Font Marketi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cardId != null && currentCard != null) {
                Text("Görünüm Önizleme", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                StandardBusinessCard(
                    name = currentCard!!.name,
                    title = currentCard!!.title,
                    company = currentCard!!.company,
                    address = currentCard!!.address,
                    email = currentCard!!.email,
                    phone1 = currentCard!!.phones,
                    phone2 = currentCard!!.phones2,
                    templateId = currentCard!!.templateId,
                    fontStyle = currentCard?.fontStyle ?: "Default",
                    qrBitmap = previewQrBitmap
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ŞABLON SEÇİMİ
            Text("Şablon Seçimi", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            val templates = listOf(1, 2, 3, 4, 5, 6)
            templates.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { id ->
                        val isPremium = id > 2
                        val hasAccess = !isPremium || subLevel != SubscriptionLevel.NONE
                        val thumb = when (id) {
                            1 -> R.drawable.card_bg_1
                            2 -> R.drawable.card_bg_2
                            3 -> R.drawable.card_bg_3
                            4 -> R.drawable.card_bg_4
                            5 -> R.drawable.card_bg_5
                            6 -> R.drawable.card_bg_6
                            else -> R.drawable.card_bg_1 
                        }
                        Card(
                            onClick = {
                                if (hasAccess && cardId != null && currentCard != null) {
                                    val updated = currentCard!!.copy(templateId = id)
                                    dbService.insertCard(updated)
                                    currentCard = updated
                                }
                            },
                            modifier = Modifier.weight(1f).height(100.dp).alpha(if (hasAccess) 1f else 0.5f),
                            border = BorderStroke(2.dp, if (currentCard?.templateId == id) Color(0xFF2C3E50) else Color.Transparent)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(painterResource(thumb), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                if (!hasAccess) Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(24.dp))
                                if (currentCard?.templateId == id) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                                Text("Tema $id", modifier = Modifier.align(Alignment.BottomStart).padding(4.dp), color = if (id == 2 || id == 3 || id == 5) Color.White else Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FONT MARKETİ
            Text("Font Seçimi", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            
            CardStyle.FONT_OPTIONS.chunked(2).forEach { rowFonts ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowFonts.forEach { font ->
                        val isPremiumFont = font.startsWith("Premium")
                        val hasFontAccess = !isPremiumFont || subLevel != SubscriptionLevel.NONE
                        val isSelected = currentCard?.fontStyle == font || (currentCard?.fontStyle == null && font == "Default")
                        
                        OutlinedButton(
                            onClick = {
                                if (hasFontAccess && cardId != null && currentCard != null) {
                                    val updated = currentCard!!.copy(fontStyle = font)
                                    dbService.insertCard(updated)
                                    currentCard = updated
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp).alpha(if (hasFontAccess) 1f else 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF2C3E50) else Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) Color(0xFFF0F2F5) else Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                                if (!hasFontAccess) {
                                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (font == "Default") "Standart" else font,
                                    fontFamily = CardStyle.getFontFamily(font),
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.Black else Color.DarkGray
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color(0xFF2C3E50))
                                }
                            }
                        }
                    }
                    if (rowFonts.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (cardId == null) {
                Spacer(modifier = Modifier.height(48.dp))
                Text("Abonelik Paketleri", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                SubscriptionOption("Aylık Paket", "₺29.99 / Ay", SubscriptionLevel.MONTHLY, authService)
                SubscriptionOption("Yıllık Paket", "₺249.99 / Yıl", SubscriptionLevel.YEARLY, authService)
            }
        }
    }
}

@Composable
fun SubscriptionOption(title: String, price: String, level: SubscriptionLevel, authService: AuthService) {
    var currentLevel by remember { mutableStateOf(authService.getSubscriptionLevel()) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(price, color = Color.Gray, fontSize = 14.sp)
            }
            Button(onClick = { authService.upgradeSubscription(level); currentLevel = level }, enabled = currentLevel != level, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))) {
                Text(if (currentLevel == level) "Mevcut" else "Satın Al")
            }
        }
    }
}
