package com.sliide.usermanagement.domain.repository

import com.sliide.usermanagement.domain.model.User

interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<List<User>>
    suspend fun createUser(name: String, email: String, gender: String, status: String): Result<User>
    suspend fun deleteUser(id: Long): Result<Unit>
}
