package com.sliide.usermanagement.domain.model

data class CreateUserRequest(
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)
