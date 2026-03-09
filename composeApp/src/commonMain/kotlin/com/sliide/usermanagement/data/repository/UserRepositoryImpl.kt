package com.sliide.usermanagement.data.repository

import com.sliide.usermanagement.data.local.UserLocalStore
import com.sliide.usermanagement.data.remote.RemoteUserSource
import com.sliide.usermanagement.data.remote.dto.toDomain
import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.errors.IOException

class UserRepositoryImpl(
    private val api: RemoteUserSource,
    private val localDataSource: UserLocalStore
) : UserRepository {

    override suspend fun getUsers(forceRefresh: Boolean): Result<List<User>> = runCatching {
        val users = api.fetchUsers().map { it.toDomain() }
        localDataSource.clearAll()
        localDataSource.insertUsers(users)
        users
    }.recoverCatching { error ->
        // Offline fallback — return cached data on initial load; surface error on manual refresh
        if (!forceRefresh && error is IOException) {
            val cached = localDataSource.getAllUsers()
            if (cached.isNotEmpty()) return@recoverCatching cached
        }
        throw error.toDomainException()
    }

    override suspend fun createUser(
        name: String,
        email: String,
        gender: String,
        status: String
    ): Result<User> = runCatching {
        api.createUser(name, email, gender, status).toDomain().also {
            localDataSource.insertUser(it)
        }
    }.mapFailure { it.toDomainException() }

    override suspend fun deleteUser(id: Long): Result<Unit> = runCatching {
        api.deleteUser(id)
        localDataSource.deleteUser(id)
    }.mapFailure { it.toDomainException() }

    private fun Throwable.toDomainException(): DomainException = when (this) {
        is DomainException -> this
        is IOException -> DomainException.NetworkException(this)
        is ResponseException -> when (response.status.value) {
            422 -> DomainException.ConflictException("This email is already registered.", this)
            in 500..599 -> DomainException.ServerException("Server error. Please try again later.", this)
            else -> DomainException.ServerException("Unexpected server response (${response.status.value}).", this)
        }
        else -> DomainException.ServerException(message ?: "Unknown error", this)
    }
}

private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    fold(onSuccess = { this }, onFailure = { Result.failure(transform(it)) })
