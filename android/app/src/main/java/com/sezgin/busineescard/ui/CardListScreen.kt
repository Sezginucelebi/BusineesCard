package com.sezgin.busineescard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.AuthService
import com.sezgin.busineescard.services.DatabaseService
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val authService = remember { AuthService(context) }
    val dbService = remember { DatabaseService(context) }
    val auth = FirebaseAuth.getInstance()
    var cards by remember { mutableStateOf(emptyList<BusinessCard>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: ""
        cards = dbService.getCards(userId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kartvizitlerim") },
                actions = {
                    IconButton(onClick = { /* Mağazaya git */ }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Yellow)
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Yeni kart ekle */ },
                containerColor = Color(0xFF2C3E50),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Paket Bilgi Barı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F2F5))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Paket: ${authService.getSubscriptionLevel().name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Kart Sayısı: ${cards.size} / ${authService.getCardLimit()}",
                    fontSize = 12.sp
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz bir kart eklemediniz.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(cards) { card ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text(card.name, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("${card.title} - ${card.company}") },
                                leadingContent = {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = Color(android.graphics.Color.parseColor("#" + card.cardColor.removePrefix("0xFF")))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(card.name.take(1).uppercase(), color = Color.White)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
