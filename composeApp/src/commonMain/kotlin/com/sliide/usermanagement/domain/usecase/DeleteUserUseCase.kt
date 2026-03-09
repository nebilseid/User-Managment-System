package com.sliide.usermanagement.domain.usecase

import com.sliide.usermanagement.domain.repository.UserRepository

class DeleteUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = repository.deleteUser(id)
}
