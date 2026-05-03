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
    val templateId: Int = 1,
    val fontStyle: String,
    val userId: String,
    // Koordinat özelleştirmeleri
    val nameX: Float? = null,
    val nameY: Float? = null,
    val titleX: Float? = null,
    val titleY: Float? = null,
    val companyX: Float? = null,
    val companyY: Float? = null,
    val detailsX: Float? = null,
    val detailsY: Float? = null,
    val qrX: Float? = null,
    val qrY: Float? = null
)
