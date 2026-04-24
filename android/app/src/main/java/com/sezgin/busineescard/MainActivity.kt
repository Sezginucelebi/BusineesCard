package com.sezgin.busineescard

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
import com.sezgin.busineescard.ui.*
import com.sezgin.busineescard.services.DatabaseService
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var vCardData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            val navController = rememberNavController()
            var currentUserId by remember { mutableStateOf("test_user") }

            NavHost(navController = navController, startDestination = "card_list") {
                composable("card_list") {
                    CardListScreen(
                        userId = currentUserId,
                        onAddCard = { navController.navigate("card_editor") },
                        onEditCard = { cardId -> navController.navigate("card_editor/$cardId") },
                        onViewCard = { cardId -> 
                            navController.navigate("card_details/$cardId")
                        },
                        onMarket = { navController.navigate("market") },
                        onLogout = { /* Çıkış işlemi */ }
                    )
                }
                composable("market") {
                    MarketScreen(onBack = { navController.popBackStack() })
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
                            vCardData = "BEGIN:VCARD\nVERSION:3.0\n" +
                                    "N:${it.name}\n" +
                                    "ORG:${it.company}\n" +
                                    "TITLE:${it.title}\n" +
                                    "TEL;TYPE=CELL:${it.phones}\n" +
                                    (if (!it.phones2.isNullOrEmpty()) "TEL;TYPE=WORK:${it.phones2}\n" else "") +
                                    "EMAIL:${it.email}\n" +
                                    "ADR:${it.address}\n" +
                                    "URL:${it.website}\n" +
                                    "END:VCARD"
                            
                            // Modern Android (API 16+) için NDEF Push Mesajı
                            // Not: Android 10+ (API 29) cihazlarda Android Beam (P2P NFC) kaldırıldı.
                            // Ancak eski API desteği için bu metod hala bazı SDK versiyonlarında mevcuttur.
                            // Eğer SDK'da bulunmuyorsa (deprecated/removed), alternatif olarak Reader Mode veya 
                            // NFC etiketine yazma mantığı kullanılır.
                        }
                    }

                    cardId?.let {
                        CardDetailsScreen(userId = currentUserId, cardId = it, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
