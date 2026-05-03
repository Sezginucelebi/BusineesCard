package com.sezgin.busineescard.services

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sezgin.busineescard.models.BusinessCard

class DatabaseService(context: Context) {
    private val prefs = context.getSharedPreferences("business_cards_db", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCards(userId: String): List<BusinessCard> {
        // Yerel veriyi dön (Hızlı yükleme için)
        val json = prefs.getString("cards_$userId", null) ?: return emptyList()
        val type = object : TypeToken<List<BusinessCard>>() {}.type
        return gson.fromJson(json, type)
    }

    // Firebase'den verileri çek ve yereli güncelle
    fun syncWithCloud(userId: String, onComplete: (List<BusinessCard>) -> Unit) {
        firestore.collection("users").document(userId).collection("cards")
            .get()
            .addOnSuccessListener { result ->
                val cards = result.toObjects(BusinessCard::class.java)
                saveCardsLocally(userId, cards)
                onComplete(cards)
            }
    }

    fun insertCard(card: BusinessCard) {
        val cards = getCards(card.userId).toMutableList()
        if (card.id == null) {
            card.id = java.util.UUID.randomUUID().toString()
            cards.add(card)
        } else {
            val index = cards.indexOfFirst { it.id == card.id }
            if (index != -1) cards[index] = card
        }
        
        // 1. Yerel Kayıt
        saveCardsLocally(card.userId, cards)
        
        // 2. Firebase Kayıt
        firestore.collection("users").document(card.userId).collection("cards")
            .document(card.id!!)
            .set(card)
    }

    fun deleteCard(userId: String, cardId: String) {
        val cards = getCards(userId).toMutableList()
        cards.removeAll { it.id == cardId }
        saveCardsLocally(userId, cards)
        
        firestore.collection("users").document(userId).collection("cards")
            .document(cardId)
            .delete()
    }

    private fun saveCardsLocally(userId: String, cards: List<BusinessCard>) {
        val json = gson.toJson(cards)
        prefs.edit().putString("cards_$userId", json).apply()
    }
    
    fun getCardById(userId: String, cardId: String): BusinessCard? {
        return getCards(userId).find { it.id == cardId }
    }
}
