package com.sezgin.busineescard.ui

import androidx.compose.foundation.background
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
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    var cards by remember { mutableStateOf(dbService.getCards(userId)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kartlarım") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış")
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
                Text("Henüz kart eklenmemiş", fontSize = 16.sp, color = Color.Gray)
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
            containerColor = Color(android.graphics.Color.parseColor("#" + card.cardColor.removePrefix("0xFF")))
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val textColor = if (card.cardColor == "0xFF2C2C2C") Color.White else Color.Black
                Text(card.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(card.title, fontSize = 14.sp, color = textColor.copy(alpha = 0.8f))
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = if (card.cardColor == "0xFF2C2C2C") Color.White else Color.Black)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
                }
            }
        }
    }
}
