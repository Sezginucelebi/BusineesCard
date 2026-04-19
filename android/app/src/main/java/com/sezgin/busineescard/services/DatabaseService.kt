package com.sezgin.busineescard.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sezgin.busineescard.models.BusinessCard
import java.util.UUID

class DatabaseService(private val context: Context) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("card_db", Context.MODE_PRIVATE)

    fun getCards(userId: String): List<BusinessCard> {
        val json = prefs.getString("cards_$userId", null) ?: return emptyList()
        val type = object : TypeToken<List<BusinessCard>>() {}.type
        return gson.fromJson(json, type)
    }

    fun insertCard(card: BusinessCard) {
        val cards = getCards(card.userId).toMutableList()
        card.id = UUID.randomUUID().toString()
        cards.add(card)
        saveCards(card.userId, cards)
    }

    fun updateCard(card: BusinessCard) {
        val cards = getCards(card.userId).toMutableList()
        val index = cards.indexOfFirst { it.id == card.id }
        if (index != -1) {
            cards[index] = card
            saveCards(card.userId, cards)
        }
    }

    fun deleteCard(id: String, userId: String) {
        val cards = getCards(userId).toMutableList()
        cards.removeAll { it.id == id }
        saveCards(userId, cards)
    }

    private fun saveCards(userId: String, cards: List<BusinessCard>) {
        val json = gson.toJson(cards)
        prefs.edit().putString("cards_$userId", json).apply()
    }
}
