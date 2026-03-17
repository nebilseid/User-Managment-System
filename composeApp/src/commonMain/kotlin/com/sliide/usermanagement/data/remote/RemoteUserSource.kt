package com.sliide.usermanagement.data.remote

import com.sliide.usermanagement.data.remote.dto.UserDto
import com.sliide.usermanagement.data.remote.dto.UsersResponse

interface RemoteUserSource {
    suspend fun fetchUsers(skip: Int, limit: Int): UsersResponse
    suspend fun createUser(name: String, email: String, gender: String, status: String): UserDto
    suspend fun deleteUser(id: Long)
}
