package com.sliide.usermanagement

import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository

class FakeUserRepository : UserRepository {

    var getUsersResult: Result<List<User>> = Result.success(emptyList())
    var createUserResult: Result<User> = Result.success(
        User(99, "New User", "new@example.com", "male", "active", 0)
    )
    var deleteUserResult: Result<Unit> = Result.success(Unit)

    val deleteCalledWithIds: MutableList<Long> = mutableListOf()
    var createCallCount: Int = 0

    override suspend fun getUsers(forceRefresh: Boolean, skip: Int, limit: Int): Result<List<User>> = getUsersResult

    override suspend fun createUser(
        name: String,
        email: String,
        gender: String,
        status: String
    ): Result<User> {
        createCallCount++
        return createUserResult
    }

    override suspend fun deleteUser(id: Long): Result<Unit> {
        deleteCalledWithIds.add(id)
        return deleteUserResult
    }
}
