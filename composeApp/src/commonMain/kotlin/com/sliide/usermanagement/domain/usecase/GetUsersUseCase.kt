package com.sliide.usermanagement.domain.usecase

import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository

class GetUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<User>> = repository.getUsers(forceRefresh)
}
