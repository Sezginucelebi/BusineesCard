package com.sezgin.busineescard.models

data class BusinessCard(
    var id: String? = null,
    val name: String,
    val title: String,
    val company: String,
    val address: String,
    val phones: String,
    val email: String,
    val website: String,
    val cardColor: String,
    val fontStyle: String,
    val userId: String
)
