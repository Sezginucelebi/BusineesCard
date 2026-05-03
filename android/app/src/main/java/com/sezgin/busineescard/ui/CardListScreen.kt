package com.sezgin.busineescard.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    onMarketForCard: (String) -> Unit,
    onMarket: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    var cards by remember { mutableStateOf(dbService.getCards(userId)) }
    var showExitDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { 
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        onLogout() 
                    }) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onDelete = {
                            dbService.deleteCard(userId, card.id ?: "")
                            cards = dbService.getCards(userId)
                        },
                        onEdit = { onEditCard(card.id ?: "") },
                        onView = { onViewCard(card.id ?: "") },
                        onCustomize = { onMarketForCard(card.id ?: "") }
                    )
                }
            }
        }
    }
}

@Composable
fun CardItem(
    card: BusinessCard, 
    onDelete: () -> Unit, 
    onEdit: () -> Unit, 
    onView: () -> Unit,
    onCustomize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Üst kısım: Kart Bilgileri (Tıklanabilir Alan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onView() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(card.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
                    Text(card.title, fontSize = 14.sp, color = Color.Gray)
                }
                Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Alt kısım: 3 Dikdörtgen Buton (Eşit Boyda)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionRectButton(
                    icon = Icons.Default.Edit,
                    label = "Düzenle",
                    color = Color(0xFF3498DB),
                    modifier = Modifier.weight(1f),
                    onClick = onEdit
                )
                ActionRectButton(
                    icon = Icons.Default.Settings,
                    label = "Tasarım",
                    color = Color(0xFFF1C40F),
                    modifier = Modifier.weight(1f),
                    onClick = onCustomize
                )
                ActionRectButton(
                    icon = Icons.Default.Delete,
                    label = "Sil",
                    color = Color(0xFFE74C3C),
                    modifier = Modifier.weight(1f),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun ActionRectButton(
    icon: ImageVector, 
    label: String, 
    color: Color, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
