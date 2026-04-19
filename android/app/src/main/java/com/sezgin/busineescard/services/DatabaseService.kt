package com.sezgin.busineescard.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sezgin.busineescard.models.BusinessCard

class DatabaseService(context: Context) {
    private val prefs = context.getSharedPreferences("business_cards_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCards(userId: String): List<BusinessCard> {
        val json = prefs.getString("cards_$userId", null) ?: return emptyList()
        val type = object : TypeToken<List<BusinessCard>>() {}.type
        return gson.fromJson(json, type)
    }

    fun insertCard(card: BusinessCard) {
        val cards = getCards(card.userId).toMutableList()
        if (card.id == null) {
            card.id = java.util.UUID.randomUUID().toString()
            cards.add(card)
        } else {
            // Update existing card
            val index = cards.indexOfFirst { it.id == card.id }
            if (index != -1) {
                cards[index] = card
            }
        }
        saveCards(card.userId, cards)
    }

    fun deleteCard(userId: String, cardId: String) {
        val cards = getCards(userId).toMutableList()
        cards.removeAll { it.id == cardId }
        saveCards(userId, cards)
    }

    private fun saveCards(userId: String, cards: List<BusinessCard>) {
        val json = gson.toJson(cards)
        prefs.edit().putString("cards_$userId", json).apply()
    }
    
    fun getCardById(userId: String, cardId: String): BusinessCard? {
        return getCards(userId).find { it.id == cardId }
    }
}
