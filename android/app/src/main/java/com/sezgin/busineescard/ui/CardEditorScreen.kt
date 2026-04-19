package com.sezgin.busineescard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.DatabaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("0xFFE3F2FD") }

    val colors = listOf("0xFFE3F2FD", "0xFFE8F5E9", "0xFFFFF3E0", "0xFFF3E5F5", "0xFFFAFAFA", "0xFF2C2C2C")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Kart") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = {
                        if (name.isNotEmpty()) {
                            val newCard = BusinessCard(
                                name = name, title = title, company = company,
                                address = address, phones = phone, email = email,
                                website = website, cardColor = selectedColor,
                                fontStyle = "Default", userId = userId
                            )
                            dbService.insertCard(newCard)
                            onBack()
                        }
                    }) {
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
            
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefon") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth())
            
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
                onClick = {
                    if (name.isNotEmpty()) {
                        val newCard = BusinessCard(
                            name = name, title = title, company = company,
                            address = address, phones = phone, email = email,
                            website = website, cardColor = selectedColor,
                            fontStyle = "Default", userId = userId
                        )
                        dbService.insertCard(newCard)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
            ) {
                Text("KAYDET")
            }
        }
    }
}
