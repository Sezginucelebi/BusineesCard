package com.sezgin.busineescard.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import com.google.gson.Gson
import com.sezgin.busineescard.BuildConfig
import com.sezgin.busineescard.models.BusinessCard
import com.sezgin.busineescard.ui.buildVCard
import com.sezgin.busineescard.ui.normalizePhoneWithCode
import com.sezgin.busineescard.ui.splitPhoneNumber

object GoogleWalletService {
    const val SAVE_PASS_REQUEST_CODE = 4107

    private val gson = Gson()

    fun isConfigured(): Boolean {
        return BuildConfig.GOOGLE_WALLET_ISSUER_ID.isNotBlank() &&
            BuildConfig.GOOGLE_WALLET_ISSUER_EMAIL.isNotBlank() &&
            BuildConfig.GOOGLE_WALLET_CLASS_SUFFIX.isNotBlank()
    }

    fun checkAvailability(context: Context, onResult: (Boolean) -> Unit) {
        if (!isConfigured()) {
            onResult(false)
            return
        }

        Pay.getClient(context)
            .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener { status ->
                onResult(status == PayApiAvailabilityStatus.AVAILABLE)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun saveBusinessCardPass(activity: Activity, card: BusinessCard) {
        if (!isConfigured()) {
            Toast.makeText(
                activity,
                "Google Wallet ayarlari eksik. Issuer bilgilerini eklemeniz gerekiyor.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val json = buildSavePassesJson(card)
        Pay.getClient(activity).savePasses(json, activity, SAVE_PASS_REQUEST_CODE)
    }

    fun handleActivityResult(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        if (requestCode != SAVE_PASS_REQUEST_CODE) return false

        when (resultCode) {
            Activity.RESULT_OK -> {
                Toast.makeText(context, "Kart Google Wallet'a eklendi.", Toast.LENGTH_LONG).show()
            }

            Activity.RESULT_CANCELED -> {
                Toast.makeText(context, "Google Wallet ekleme islemi iptal edildi.", Toast.LENGTH_SHORT).show()
            }

            PayClient.SavePassesResult.SAVE_ERROR -> {
                val apiMessage = data?.getStringExtra(PayClient.EXTRA_API_ERROR_MESSAGE)
                Toast.makeText(
                    context,
                    apiMessage ?: "Google Wallet kaydetme hatasi olustu.",
                    Toast.LENGTH_LONG
                ).show()
            }

            PayClient.SavePassesResult.API_UNAVAILABLE -> {
                Toast.makeText(
                    context,
                    "Google Wallet bu cihazda veya hesapta kullanilamiyor.",
                    Toast.LENGTH_LONG
                ).show()
            }

            PayClient.SavePassesResult.INTERNAL_ERROR -> {
                Toast.makeText(
                    context,
                    "Google Wallet gecici bir hata verdi. Tekrar deneyin.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    context,
                    "Google Wallet sonucu anlasilamadi: $resultCode",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return true
    }

    private fun buildSavePassesJson(card: BusinessCard): String {
        val issuerId = BuildConfig.GOOGLE_WALLET_ISSUER_ID
        val classId = "$issuerId.${BuildConfig.GOOGLE_WALLET_CLASS_SUFFIX}"
        val objectId = "$issuerId.${buildObjectSuffix(card)}"
        val (phone1Code, phone1Local) = splitPhoneNumber(card.phones)
        val phone1 = normalizePhoneWithCode(phone1Code, phone1Local)
        val phone2 = card.phones2
            ?.takeIf { it.isNotBlank() }
            ?.let {
                val (code, local) = splitPhoneNumber(it)
                normalizePhoneWithCode(code, local)
            }
        val qrValue = buildVCard(card, phone1, phone2)

        val genericClass = mapOf(
            "id" to classId,
            "issuerName" to BuildConfig.GOOGLE_WALLET_ISSUER_NAME,
            "reviewStatus" to "UNDER_REVIEW"
        )

        val textModules = buildList {
            if (card.address.isNotBlank()) {
                add(
                    mapOf(
                        "id" to "address",
                        "header" to "Adres",
                        "body" to card.address
                    )
                )
            }
            if (card.email.isNotBlank()) {
                add(
                    mapOf(
                        "id" to "email",
                        "header" to "E-posta",
                        "body" to card.email
                    )
                )
            }
            if (phone1.isNotBlank()) {
                add(
                    mapOf(
                        "id" to "phone1",
                        "header" to "Telefon",
                        "body" to phone1
                    )
                )
            }
            if (!phone2.isNullOrBlank()) {
                add(
                    mapOf(
                        "id" to "phone2",
                        "header" to "Telefon 2",
                        "body" to phone2
                    )
                )
            }
            if (card.website.isNotBlank()) {
                add(
                    mapOf(
                        "id" to "website",
                        "header" to "Web sitesi",
                        "body" to card.website
                    )
                )
            }
        }

        val genericObject = linkedMapOf<String, Any>(
            "id" to objectId,
            "classId" to classId,
            "state" to "ACTIVE",
            "cardTitle" to localized(card.company.ifBlank { BuildConfig.GOOGLE_WALLET_ISSUER_NAME }),
            "header" to localized(card.name),
            "subheader" to localized(card.title.ifBlank { card.company }),
            "barcode" to mapOf(
                "type" to "QR_CODE",
                "value" to qrValue,
                "alternateText" to phone1
            ),
            "hexBackgroundColor" to "#F4F1EA",
            "textModulesData" to textModules
        )

        if (card.website.isNotBlank()) {
            genericObject["linksModuleData"] = mapOf(
                "uris" to listOf(
                    mapOf(
                        "uri" to card.website,
                        "description" to "Web sitesi"
                    )
                )
            )
        }

        val payload = mapOf(
            "iss" to BuildConfig.GOOGLE_WALLET_ISSUER_EMAIL,
            "aud" to "google",
            "typ" to "savetowallet",
            "origins" to emptyList<String>(),
            "payload" to mapOf(
                "genericClasses" to listOf(genericClass),
                "genericObjects" to listOf(genericObject)
            )
        )

        return gson.toJson(payload)
    }

    private fun localized(value: String): Map<String, Any> {
        return mapOf(
            "defaultValue" to mapOf(
                "language" to "tr-TR",
                "value" to value
            )
        )
    }

    private fun buildObjectSuffix(card: BusinessCard): String {
        val base = "${card.userId}_${card.id ?: card.name}"
        return base
            .lowercase()
            .replace(Regex("[^a-z0-9._-]"), "_")
            .take(50)
            .ifBlank { "business_card" }
    }
}
