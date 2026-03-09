package com.sliide.usermanagement.domain.util

fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()

    // RFC 5321 length limits
    if (trimmed.length > 254) return false

    val atIndex = trimmed.indexOf('@')
    if (atIndex < 1 || atIndex != trimmed.lastIndexOf('@')) return false

    val local = trimmed.substring(0, atIndex)
    val domain = trimmed.substring(atIndex + 1)

    // Local part constraints
    if (local.length > 64) return false
    if (local.startsWith('.') || local.endsWith('.')) return false
    if (local.contains("..")) return false

    // Domain constraints
    if (domain.startsWith('.') || domain.startsWith('-')) return false
    if (domain.contains("..")) return false

    val emailRegex = Regex(
        "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    )
    return trimmed.matches(emailRegex)
}

fun isValidName(name: String): Boolean {
    val trimmed = name.trim()
    return trimmed.length >= 2 && trimmed.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
}
