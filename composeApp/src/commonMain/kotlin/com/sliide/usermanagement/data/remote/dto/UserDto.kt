package com.sliide.usermanagement.data.remote.dto

import com.sliide.usermanagement.domain.model.User
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class UserDto(
    val id: Long,
    val firstName: String,
    val lastName: String = "",
    val email: String = "",
    val gender: String = "",
    val status: String = "active"
)

@Serializable
data class UsersResponse(val users: List<UserDto>)

@Serializable
data class CreateUserRequest(
    val firstName: String,
    val email: String,
    val gender: String,
    val status: String
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = if (lastName.isBlank()) firstName else "$firstName $lastName",
    email = email,
    gender = gender,
    status = status,
    createdAt = Clock.System.now().toEpochMilliseconds()
)
