package com.sezgin.busineescard

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.sezgin.busineescard.services.GoogleWalletService
import com.sezgin.busineescard.ui.*
import com.sezgin.busineescard.services.DatabaseService
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            val navController = rememberNavController()
            val auth = remember { FirebaseAuth.getInstance() }
            var currentUserId by remember { mutableStateOf(auth.currentUser?.uid ?: "") }
            
            val startScreen = if (currentUserId.isNotEmpty()) "card_list" else "login"

            NavHost(navController = navController, startDestination = startScreen) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { 
                            currentUserId = auth.currentUser?.uid ?: ""
                            navController.navigate("card_list") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate("register") }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            currentUserId = auth.currentUser?.uid ?: ""
                            navController.navigate("card_list") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }
                composable("card_list") {
                    CardListScreen(
                        userId = currentUserId,
                        onAddCard = { navController.navigate("card_editor") },
                        onEditCard = { cardId -> navController.navigate("card_editor/$cardId") },
                        onViewCard = { cardId -> navController.navigate("card_details/$cardId") },
                        onMarketForCard = { cardId -> navController.navigate("market/$cardId") },
                        onMarket = { navController.navigate("market/null") },
                        onLogout = { 
                            currentUserId = ""
                            navController.navigate("login") {
                                popUpTo("card_list") { inclusive = true }
                            }
                        }
                    )
                }
                composable("market/{cardId}") { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getString("cardId")
                    MarketScreen(
                        userId = currentUserId,
                        cardId = if (cardId == "null") null else cardId,
                        onBack = { navController.popBackStack() }
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
                    val dbService = remember { DatabaseService(this@MainActivity) }
                    val card = remember(cardId) { cardId?.let { dbService.getCardById(currentUserId, it) } }
                    
                    LaunchedEffect(card) {
                        card?.let {
                            val vCard = "BEGIN:VCARD\nVERSION:3.0\n" +
                                    "N:${it.name}\n" +
                                    "ORG:${it.company}\n" +
                                    "TITLE:${it.title}\n" +
                                    "TEL;TYPE=CELL:${it.phones}\n" +
                                    (if (!it.phones2.isNullOrEmpty()) "TEL;TYPE=WORK:${it.phones2}\n" else "") +
                                    "EMAIL:${it.email}\n" +
                                    "ADR:${it.address}\n" +
                                    "URL:${it.website}\n" +
                                    "END:VCARD"
                            
                            setupNfcSharing(vCard)
                        }
                    }

                    cardId?.let { id ->
                        CardDetailsScreen(
                            userId = currentUserId, 
                            cardId = id, 
                            onBack = { navController.popBackStack() },
                            onOpenMarket = { navController.navigate("market/$id") }
                        )
                    }
                }
            }
        }
    }

    private fun setupNfcSharing(vCard: String) {
        if (nfcAdapter == null) return
        
        // Android 10 (API 29) ve sonrasında Android Beam (P2P NFC) kaldırılmıştır.
        // Modern cihazlarda NFC paylaşımı için ya bir NFC etiketine yazma ya da 
        // QR kod gibi alternatifler kullanılır.
        // Eski cihazlar için NDEF push mesajı oluşturulabilir ancak derleme hatasını
        // önlemek için bu özellik modern Android standartlarına göre pasifize edilmiştir.
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (GoogleWalletService.handleActivityResult(this, requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
