package com.sliide.usermanagement.data.local

import com.sliide.usermanagement.domain.model.User

interface UserLocalStore {
    fun getAllUsers(): List<User>
    fun insertUsers(users: List<User>)
    fun insertUser(user: User)
    fun deleteUser(id: Long)
    fun clearAll()
}
