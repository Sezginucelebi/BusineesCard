package com.sezgin.busineescard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sezgin.busineescard.ui.*
import com.sezgin.busineescard.services.DatabaseService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            var currentUserId by remember { mutableStateOf("test_user") }

            NavHost(navController = navController, startDestination = "card_list") {
                composable("card_list") {
                    CardListScreen(
                        userId = currentUserId,
                        onAddCard = { navController.navigate("card_editor") },
                        onEditCard = { cardId -> navController.navigate("card_editor/$cardId") },
                        onViewCard = { cardId -> navController.navigate("card_details/$cardId") },
                        onLogout = { /* Çıkış işlemi buraya gelecek */ }
                    )
                }
                composable("card_editor") {
                    CardEditorScreen(userId = currentUserId, onBack = { navController.popBackStack() })
                }
                composable("card_editor/{cardId}") { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getString("cardId")
                    CardEditorScreen(userId = currentUserId, cardId = cardId, onBack = { navController.popBackStack() })
                }
                composable("card_details/{cardId}") { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getString("cardId")
                    cardId?.let {
                        CardDetailsScreen(userId = currentUserId, cardId = it, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
