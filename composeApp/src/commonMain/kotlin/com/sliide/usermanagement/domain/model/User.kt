package com.sliide.usermanagement.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val gender: String,
    val status: String,
    val createdAt: Long // epoch millis — server creation time, fallback to Clock.System.now()
)
