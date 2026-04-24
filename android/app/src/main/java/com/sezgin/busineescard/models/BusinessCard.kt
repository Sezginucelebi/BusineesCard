package com.sezgin.busineescard.models

data class BusinessCard(
    var id: String? = null,
    val name: String,
    val title: String,
    val company: String,
    val address: String,
    val phones: String,
    val phones2: String? = null,
    val email: String,
    val website: String,
    val cardColor: String,
    val templateId: Int = 1, // 1, 2 veya 3
    val fontStyle: String,
    val userId: String
)
