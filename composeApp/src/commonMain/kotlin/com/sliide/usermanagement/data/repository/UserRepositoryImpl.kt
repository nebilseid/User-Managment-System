package com.sliide.usermanagement.data.repository

import com.sliide.usermanagement.data.local.UserLocalStore
import com.sliide.usermanagement.data.remote.RemoteUserSource
import com.sliide.usermanagement.data.remote.dto.toDomain
import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlin.time.Clock

class UserRepositoryImpl(
    private val api: RemoteUserSource,
    private val localDataSource: UserLocalStore
) : UserRepository {

    override suspend fun getUsers(forceRefresh: Boolean, skip: Int, limit: Int): Result<List<User>> = runCatching {
        if (skip == 0) {
            // Probe total count (limit=0 returns metadata only, no user payload).
            val total = api.fetchUsers(skip = 0, limit = 0).total
            val lastPageSkip = maxOf(0, total - limit)
            val serverUsers = api.fetchUsers(skip = lastPageSkip, limit = limit).users.map { it.toDomain() }
            // Replace server rows only — locally-created users (id > DUMMYJSON_MAX_ID) are never
            // touched, so they survive pull-to-refresh and app restarts automatically.
            localDataSource.clearServerUsers()
            localDataSource.insertUsers(serverUsers)
            val localOnly = localDataSource.getAllUsers().filter { it.id > DUMMYJSON_MAX_ID }
            localOnly + serverUsers
        } else {
            api.fetchUsers(skip, limit).users.map { it.toDomain() }
        }
    }.recoverCatching { error ->
        // Offline fallback — only applies to initial load (skip == 0); pagination requires network
        if (!forceRefresh && skip == 0 && error is IOException) {
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
        val user = api.createUser(name, email, gender, status).toDomain()
            .copy(id = Clock.System.now().toEpochMilliseconds(), status = status)
        localDataSource.insertUser(user)
        user
    }.mapFailure { it.toDomainException() }

    override suspend fun deleteUser(id: Long): Result<Unit> = runCatching {
        api.deleteUser(id)
        localDataSource.deleteUser(id)
    }.mapFailure { it.toDomainException() }

    companion object {
        // DummyJSON user ids go up to 208. Locally-created users get Clock timestamp ids (>>208).
        private const val DUMMYJSON_MAX_ID = 208L
    }

    private fun Throwable.toDomainException(): DomainException {
        val exception = when (this) {
            is DomainException -> this
            is IOException -> DomainException.NetworkException(this)
            is ResponseException -> when (response.status.value) {
                422 -> DomainException.ConflictException("This email is already registered.", this)
                in 500..599 -> DomainException.ServerException(
                    "Server error. Please try again later.",
                    this
                )

                else -> DomainException.ServerException(
                    "Unexpected server response (${response.status.value}).",
                    this
                )
            }

            else -> DomainException.ServerException(message ?: "Unknown error", this)
        }
        return exception
    }
}

private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    fold(onSuccess = { this }, onFailure = { Result.failure(transform(it)) })
