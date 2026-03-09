package com.sliide.usermanagement.data.repository

import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository
import kotlin.time.Clock
import kotlinx.coroutines.delay

class MockUserRepository : UserRepository {

    private val users = mutableListOf(
        User(1, "Alice Johnson", "alice.johnson@example.com", "female", "active", 0),
        User(2, "Ben Carter", "ben.carter@example.com", "male", "active", 0),
        User(3, "Clara Diaz", "clara.diaz@example.com", "female", "inactive", 0),
        User(4, "David Kim", "david.kim@example.com", "male", "active", 0),
        User(5, "Eva Müller", "eva.muller@example.com", "female", "active", 0),
        User(6, "Frank Osei", "frank.osei@example.com", "male", "inactive", 0),
        User(7, "Grace Liu", "grace.liu@example.com", "female", "active", 0),
        User(8, "Hassan Ali", "hassan.ali@example.com", "male", "active", 0),
        User(9, "Isabelle Russo", "isabelle.russo@example.com", "female", "inactive", 0),
        User(10, "James Wright", "james.wright@example.com", "male", "active", 0),
    )

    private var nextId = 11L

    override suspend fun getUsers(forceRefresh: Boolean): Result<List<User>> {
        delay(800) // simulate network latency
        return Result.success(users.toList())
    }

    override suspend fun createUser(
        name: String,
        email: String,
        gender: String,
        status: String
    ): Result<User> {
        delay(500)
        if (users.any { it.email == email }) {
            return Result.failure(DomainException.ConflictException("Email already in use"))
        }
        val user = User(nextId++, name, email, gender, status, Clock.System.now().toEpochMilliseconds())
        users.add(0, user)
        return Result.success(user)
    }

    override suspend fun deleteUser(id: Long): Result<Unit> {
        delay(300)
        users.removeAll { it.id == id }
        return Result.success(Unit)
    }

}
