package com.sezgin.busineescard.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sezgin.busineescard.R
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.DatabaseService
import com.sezgin.busineescard.utils.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(userId: String, cardId: String? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    
    val existingCard = remember { if (cardId != null) dbService.getCardById(userId, cardId) else null }

    var name by remember { mutableStateOf(existingCard?.name ?: "") }
    var title by remember { mutableStateOf(existingCard?.title ?: "") }
    var company by remember { mutableStateOf(existingCard?.company ?: "") }
    
    val savedPhone = existingCard?.phones ?: "+90 "
    var countryCode by remember { mutableStateOf(if (savedPhone.startsWith("+")) savedPhone.split(" ").firstOrNull() ?: "+90" else "+90") }
    var phoneNumber by remember { mutableStateOf(if (savedPhone.contains(" ")) savedPhone.split(" ").drop(1).joinToString(" ") else savedPhone.removePrefix("+90").trim()) }
    
    val savedPhone2 = existingCard?.phones2 ?: ""
    var countryCode2 by remember { mutableStateOf(if (savedPhone2.startsWith("+")) savedPhone2.split(" ").firstOrNull() ?: "+90" else "+90") }
    var phoneNumber2 by remember { mutableStateOf(if (savedPhone2.contains(" ")) savedPhone2.split(" ").drop(1).joinToString(" ") else "") }
    
    var isPhones2Visible by remember { mutableStateOf(!existingCard?.phones2.isNullOrEmpty()) }
    
    var email by remember { mutableStateOf(existingCard?.email ?: "") }
    var website by remember { mutableStateOf(existingCard?.website ?: "") }
    var address by remember { mutableStateOf(existingCard?.address ?: "") }
    val templateId = existingCard?.templateId ?: 1
    val selectedColor = existingCard?.cardColor ?: "0xFFE3F2FD"

    // Mockup için dinamik veriler
    val displayPhone = "$countryCode $phoneNumber"
    val displayPhone2 = if (isPhones2Visible && phoneNumber2.isNotEmpty()) "$countryCode2 $phoneNumber2" else null

    // Mockup için QR hazırlığı
    val qrContent = "BEGIN:VCARD\nVERSION:3.0\nN:$name\nORG:$company\nTITLE:$title\nTEL:$displayPhone\n" +
            (if (!displayPhone2.isNullOrEmpty()) "TEL:$displayPhone2\n" else "") +
            "EMAIL:$email\nADR:$address\nURL:$website\nEND:VCARD"
    val qrBitmap = remember(qrContent) { QRCodeGenerator.generateQRCode(qrContent, 512) }

    fun getFlagEmoji(code: String): String {
        return when (code.trim()) {
            "+90" -> "🇹🇷"
            "+1" -> "🇺🇸"
            "+44" -> "🇬🇧"
            "+49" -> "🇩🇪"
            "+33" -> "🇫🇷"
            else -> "🏳️"
        }
    }

    fun saveCard() {
        if (name.isNotEmpty()) {
            val cardToSave = BusinessCard(
                id = existingCard?.id,
                name = name, title = title, company = company,
                address = address, phones = displayPhone, 
                phones2 = displayPhone2,
                email = email,
                website = website, 
                cardColor = selectedColor,
                templateId = templateId,
                fontStyle = "Default", userId = userId
            )
            dbService.insertCard(cardToSave)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == null) "Yeni Kart" else "Kartı Düzenle") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = { saveCard() }) { Icon(Icons.Default.Check, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // CANLI ÖNİZLEME (MOCKUP)
            Text("Kart Önizleme", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1050f / 600f)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
            ) {
                val bgResId = when(templateId) {
                    1 -> R.drawable.card_bg_1
                    2 -> R.drawable.card_bg_2
                    else -> R.drawable.card_bg_3
                }
                Image(painterResource(bgResId), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
                
                Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    val textColor = if (templateId == 2) Color.White else Color(0xFF2C3E50)
                    
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text(name.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(title, fontSize = 10.sp, color = textColor.copy(0.8f), maxLines = 1)
                        Text(company, fontSize = 10.sp, color = textColor.copy(0.8f), maxLines = 1)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                            val smallText = textColor.copy(0.7f)
                            if (address.isNotEmpty()) {
                                Text(address, fontSize = 8.sp, lineHeight = 10.sp, color = smallText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(2.dp))
                            }
                            if (email.isNotEmpty()) {
                                Text(email, fontSize = 8.sp, color = smallText, maxLines = 1)
                            }
                            Text("${getFlagEmoji(countryCode)} $displayPhone", fontSize = 8.sp, color = smallText, maxLines = 1)
                            displayPhone2?.let {
                                Text("${getFlagEmoji(countryCode2)} $it", fontSize = 8.sp, color = smallText, maxLines = 1)
                            }
                        }
                    }
                    
                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.size(99.dp).align(Alignment.BottomEnd).shadow(2.dp, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp).fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Bilgileri Düzenle", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Ad Soyad") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Ünvan") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = company, 
                onValueChange = { company = it }, 
                label = { Text("Şirket") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("İletişim", fontWeight = FontWeight.SemiBold)
                if (!isPhones2Visible) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { isPhones2Visible = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Text(" Telefon Ekle")
                    }
                }
            }
            
            // Telefon 1
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = countryCode,
                    onValueChange = { countryCode = it },
                    label = { Text("Kod") },
                    leadingIcon = { Text(getFlagEmoji(countryCode), modifier = Modifier.padding(start = 8.dp)) },
                    modifier = Modifier.weight(0.35f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.weight(0.65f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )
            }
            
            // Telefon 2
            if (isPhones2Visible) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = countryCode2,
                        onValueChange = { countryCode2 = it },
                        label = { Text("Kod") },
                        leadingIcon = { Text(getFlagEmoji(countryCode2), modifier = Modifier.padding(start = 8.dp)) },
                        modifier = Modifier.weight(0.35f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phoneNumber2,
                        onValueChange = { phoneNumber2 = it },
                        label = { Text("2. Telefon") },
                        modifier = Modifier.weight(0.65f),
                        trailingIcon = {
                            IconButton(onClick = { isPhones2Visible = false; phoneNumber2 = "" }) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email, 
                onValueChange = { email = it }, 
                label = { Text("E-posta") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address, 
                onValueChange = { address = it }, 
                label = { Text("Adres") }, 
                modifier = Modifier.fillMaxWidth(), 
                minLines = 2
                // Adres için Next eklenmedi, varsayılan Enter alt satıra geçer
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = website, 
                onValueChange = { website = it }, 
                label = { Text("Web Sitesi") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { saveCard() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
            ) {
                Text("KAYDET", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
