package com.sezgin.busineescard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.DatabaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(userId: String, cardId: String? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    
    // Mevcut kartı yükle (eğer düzenleme modundaysak)
    val existingCard = remember { if (cardId != null) dbService.getCardById(userId, cardId) else null }

    var name by remember { mutableStateOf(existingCard?.name ?: "") }
    var title by remember { mutableStateOf(existingCard?.title ?: "") }
    var company by remember { mutableStateOf(existingCard?.company ?: "") }
    
    // Telefonu parçala
    val savedPhone = existingCard?.phones ?: "+90 "
    var countryCode by remember { mutableStateOf(savedPhone.split(" ").firstOrNull() ?: "+90") }
    var phoneNumber by remember { mutableStateOf(if (savedPhone.contains(" ")) savedPhone.split(" ").drop(1).joinToString(" ") else "") }
    
    var email by remember { mutableStateOf(existingCard?.email ?: "") }
    var website by remember { mutableStateOf(existingCard?.website ?: "") }
    var address by remember { mutableStateOf(existingCard?.address ?: "") }
    var selectedColor by remember { mutableStateOf(existingCard?.cardColor ?: "0xFFE3F2FD") }

    val colors = listOf("0xFFE3F2FD", "0xFFE8F5E9", "0xFFFFF3E0", "0xFFF3E5F5", "0xFFFAFAFA", "0xFF2C2C2C")

    fun saveCard() {
        if (name.isNotEmpty()) {
            val fullPhone = "$countryCode $phoneNumber"
            val cardToSave = BusinessCard(
                id = existingCard?.id, // ID varsa güncelleme, yoksa yeni kayıt
                name = name, title = title, company = company,
                address = address, phones = fullPhone, email = email,
                website = website, cardColor = selectedColor,
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
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { saveCard() }) {
                        Icon(Icons.Default.Check, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ad Soyad") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Ünvan") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Şirket") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("İletişim Bilgileri", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = countryCode,
                    onValueChange = { countryCode = it },
                    label = { Text("Kod") },
                    modifier = Modifier.weight(0.3f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefon Numarası") },
                    modifier = Modifier.weight(0.7f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Web Sitesi") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kart Rengi", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                colors.forEach { colorHex ->
                    val color = Color(android.graphics.Color.parseColor("#" + colorHex.removePrefix("0xFF")))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .background(color, CircleShape)
                            .clickable { selectedColor = colorHex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == colorHex) {
                            Icon(Icons.Default.Check, null, tint = if (colorHex == "0xFF2C2C2C") Color.White else Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { saveCard() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
            ) {
                Text(if (cardId == null) "KAYDET" else "GÜNCELLE")
            }
        }
    }
}
