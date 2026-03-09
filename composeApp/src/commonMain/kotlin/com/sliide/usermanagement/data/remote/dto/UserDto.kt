package com.sliide.usermanagement.data.remote.dto

import com.sliide.usermanagement.domain.model.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val gender: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    gender = gender,
    status = status,
    createdAt = createdAt
        ?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() }
        ?: Clock.System.now().toEpochMilliseconds()
)
