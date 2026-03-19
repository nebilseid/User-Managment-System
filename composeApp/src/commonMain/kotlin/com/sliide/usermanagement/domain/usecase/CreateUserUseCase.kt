package com.sliide.usermanagement.domain.usecase

import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.CreateUserRequest
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository
import com.sliide.usermanagement.domain.util.isValidEmail
import com.sliide.usermanagement.domain.util.isValidName

class CreateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(request: CreateUserRequest): Result<User> {
        if (!isValidName(request.name)) return Result.failure(DomainException.ValidationException("Invalid name"))
        if (!isValidEmail(request.email)) return Result.failure(DomainException.ValidationException("Invalid email address"))
        if (request.gender.isBlank()) return Result.failure(DomainException.ValidationException("Gender is required"))
        if (request.status.isBlank()) return Result.failure(DomainException.ValidationException("Status is required"))
        return repository.createUser(request.name, request.email, request.gender, request.status)
    }
}
