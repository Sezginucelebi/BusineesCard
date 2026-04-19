package com.sezgin.busineescard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    
                    NavHost(
                        navController = navController,
                        startDestination = if (currentUser != null) "card_list" else "login"
                    ) {
                        composable("login") {
                            // LoginView'ın Jetpack Compose hali buraya gelecek
                        }
                        composable("card_list") {
                            // CardListView'ın Jetpack Compose hali buraya gelecek
                        }
                    }
                }
            }
        }
    }
}
