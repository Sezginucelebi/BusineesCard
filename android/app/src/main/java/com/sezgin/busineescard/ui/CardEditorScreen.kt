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
    
    val savedPhone = splitPhoneNumber(existingCard?.phones.orEmpty())
    var countryCode by remember { mutableStateOf(savedPhone.first) }
    var phoneNumber by remember { mutableStateOf(savedPhone.second) }
    
    val savedPhone2 = splitPhoneNumber(existingCard?.phones2.orEmpty())
    var countryCode2 by remember { mutableStateOf(savedPhone2.first) }
    var phoneNumber2 by remember { mutableStateOf(savedPhone2.second) }
    
    var isPhones2Visible by remember { mutableStateOf(!existingCard?.phones2.isNullOrEmpty()) }
    
    var email by remember { mutableStateOf(existingCard?.email ?: "") }
    var website by remember { mutableStateOf(existingCard?.website ?: "") }
    var address by remember { mutableStateOf(existingCard?.address ?: "") }
    val templateId = existingCard?.templateId ?: 1
    val fontStyle = existingCard?.fontStyle ?: "Default"
    val selectedColor = existingCard?.cardColor ?: "0xFFE3F2FD"

    // Mockup için dinamik veriler
    val displayPhone = normalizePhoneWithCode(countryCode, phoneNumber)
    val displayPhone2 = if (isPhones2Visible && phoneNumber2.isNotEmpty()) {
        normalizePhoneWithCode(countryCode2, phoneNumber2)
    } else null

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
                fontStyle = fontStyle,
                userId = userId
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
            StandardBusinessCard(
                name = name,
                title = title,
                company = company,
                address = address,
                email = email,
                phone1 = displayPhone,
                phone2 = displayPhone2,
                templateId = templateId,
                fontStyle = fontStyle,
                qrBitmap = qrBitmap
            )

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
                    onValueChange = { phoneNumber = formatPhoneLocal(it) },
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
                        onValueChange = { phoneNumber2 = formatPhoneLocal(it) },
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
