package com.example.model

enum class AppLanguage(val code: String, val buttonLabel: String) {
    EN("en", "العربية"),
    AR("ar", "EN")
}

fun tr(lang: AppLanguage, en: String, ar: String): String =
    if (lang == AppLanguage.AR) ar else en
