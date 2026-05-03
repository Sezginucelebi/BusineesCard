package com.sezgin.busineescard.ui

private val PHONE_GROUPS = listOf(3, 3, 2, 2)

fun formatPhoneLocal(raw: String): String {
    // Sadece rakamları al
    val digits = raw.filter(Char::isDigit)
    if (digits.isEmpty()) return ""

    val parts = mutableListOf<String>()
    var index = 0

    for (groupSize in PHONE_GROUPS) {
        if (index >= digits.length) break
        val end = (index + groupSize).coerceAtMost(digits.length)
        parts.add(digits.substring(index, end))
        index = end
    }

    if (index < digits.length) {
        parts.add(digits.substring(index))
    }

    // Grupları sadece "-" ile birleştir (boşluksuz)
    return parts.joinToString("-")
}

fun normalizePhoneWithCode(countryCode: String, rawLocal: String): String {
    val local = formatPhoneLocal(rawLocal)
    val code = countryCode.trim()
    
    if (code.isBlank()) return local
    if (local.isBlank()) return code
    
    // Ülke kodu ile numara arasına boşluk, numara içine "-" koyar
    // Sonuç: +90 555-555-55-55
    return "$code $local"
}

fun splitPhoneNumber(phone: String, defaultCountryCode: String = "+90"): Pair<String, String> {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return defaultCountryCode to ""

    return if (trimmed.startsWith("+")) {
        // Ülke kodunu (ilk boşluğa veya tireye kadar) ayır
        val parts = trimmed.split(Regex("[\\s-]+"), limit = 2)
        val code = parts.firstOrNull()?.takeIf { it.startsWith("+") } ?: defaultCountryCode
        val localRaw = if (parts.size > 1) parts[1] else ""
        code to formatPhoneLocal(localRaw)
    } else {
        defaultCountryCode to formatPhoneLocal(trimmed)
    }
}
