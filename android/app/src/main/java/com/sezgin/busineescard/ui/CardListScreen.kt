package com.sezgin.busineescard.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.services.DatabaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    userId: String,
    onAddCard: () -> Unit,
    onEditCard: (String) -> Unit,
    onViewCard: (String) -> Unit,
    onMarket: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    var cards by remember { mutableStateOf(dbService.getCards(userId)) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Geri tuşuna basıldığında çıkış diyaloğunu göster
    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Çıkış") },
            text = { Text("Uygulamadan çıkmak istiyor musunuz?") },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) {
                    Text("EVET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("HAYIR")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kartlarım", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onMarket) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Market")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Oturumu Kapat")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard, containerColor = Color(0xFF2C3E50), contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }
    ) { padding ->
        if (cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("Henüz kart eklenmemiş", fontSize = 16.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onDelete = {
                            dbService.deleteCard(userId, card.id ?: "")
                            cards = dbService.getCards(userId)
                        },
                        onEdit = { onEditCard(card.id ?: "") },
                        onView = { onViewCard(card.id ?: "") }
                    )
                }
            }
        }
    }
}

@Composable
fun CardItem(card: BusinessCard, onDelete: () -> Unit, onEdit: () -> Unit, onView: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onView() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA) // Liste öğeleri için daha nötr bir renk
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
                Text(card.title, fontSize = 14.sp, color = Color.Gray)
                Text(card.company, fontSize = 12.sp, color = Color.LightGray)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color(0xFF2C3E50))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}
