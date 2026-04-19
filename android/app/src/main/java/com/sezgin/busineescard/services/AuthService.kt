package com.sezgin.busineescard.services

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

enum class SubscriptionLevel {
    NONE, MONTHLY, YEARLY
}

class AuthService(private val context: Context) {
    private val auth = FirebaseAuth.instance

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun getSubscriptionLevel(): SubscriptionLevel {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val level = prefs.getString("sub_level", "NONE") ?: "NONE"
        return SubscriptionLevel.valueOf(level)
    }

    fun getCardLimit(): Int {
        return when (getSubscriptionLevel()) {
            SubscriptionLevel.MONTHLY -> 2
            SubscriptionLevel.YEARLY -> 10
            SubscriptionLevel.NONE -> 1
        }
    }

    suspend fun upgradeSubscription(level: SubscriptionLevel) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("sub_level", level.name).apply()
    }
}
