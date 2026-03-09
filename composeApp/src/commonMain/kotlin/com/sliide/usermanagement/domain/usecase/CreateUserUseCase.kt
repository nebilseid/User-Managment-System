package com.sliide.usermanagement.domain.usecase

import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository
import com.sliide.usermanagement.domain.util.isValidEmail
import com.sliide.usermanagement.domain.util.isValidName

class CreateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        gender: String,
        status: String
    ): Result<User> {
        if (!isValidName(name)) return Result.failure(IllegalArgumentException("Invalid name"))
        if (!isValidEmail(email)) return Result.failure(IllegalArgumentException("Invalid email address"))
        if (gender.isBlank()) return Result.failure(IllegalArgumentException("Gender is required"))
        if (status.isBlank()) return Result.failure(IllegalArgumentException("Status is required"))
        return repository.createUser(name, email, gender, status)
    }
}
