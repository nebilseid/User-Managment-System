package com.sliide.usermanagement.data.repository

import com.sliide.usermanagement.data.local.UserLocalStore
import com.sliide.usermanagement.data.remote.RemoteUserSource
import com.sliide.usermanagement.data.remote.dto.UserDto
import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.User
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserRepositoryImplTest {

    // --- fakes ---

    private class FakeRemoteUserSource(
        private val usersResult: () -> List<UserDto> = { emptyList() }
    ) : RemoteUserSource {
        override suspend fun fetchUsers(): List<UserDto> = usersResult()
        override suspend fun createUser(name: String, email: String, gender: String, status: String): UserDto =
            error("unexpected call")
        override suspend fun deleteUser(id: Long) = Unit
    }

    private class InMemoryLocalStore(preloaded: List<User> = emptyList()) : UserLocalStore {
        private val store = preloaded.toMutableList()
        var clearAllCalled = false

        override fun getAllUsers(): List<User> = store.toList()
        override fun insertUsers(users: List<User>) { store.addAll(users) }
        override fun insertUser(user: User) { store.add(user) }
        override fun deleteUser(id: Long) { store.removeAll { it.id == id } }
        override fun clearAll() { clearAllCalled = true; store.clear() }
    }

    private fun user(id: Long) = User(id, "User $id", "u$id@example.com", "male", "active", 0L)
    private fun userDto(id: Long) = UserDto(id, "User $id", "u$id@example.com", "male", "active", null)

    // --- offline fallback tests ---

    @Test
    fun `getUsers returns cached data when API throws IOException`() = runTest {
        val cached = listOf(user(1), user(2))
        val api = FakeRemoteUserSource { throw IOException("no network") }
        val local = InMemoryLocalStore(preloaded = cached)
        val repo = UserRepositoryImpl(api, local)

        val result = repo.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(cached, result.getOrThrow())
    }

    @Test
    fun `getUsers throws NetworkException when API fails and cache is empty`() = runTest {
        val api = FakeRemoteUserSource { throw IOException("no network") }
        val local = InMemoryLocalStore()
        val repo = UserRepositoryImpl(api, local)

        val result = repo.getUsers()

        assertTrue(result.isFailure)
        assertIs<DomainException.NetworkException>(result.exceptionOrNull())
    }

    @Test
    fun `getUsers with forceRefresh throws NetworkException even when cache is non-empty`() = runTest {
        val cached = listOf(user(1), user(2))
        val api = FakeRemoteUserSource { throw IOException("no network") }
        val local = InMemoryLocalStore(preloaded = cached)
        val repo = UserRepositoryImpl(api, local)

        val result = repo.getUsers(forceRefresh = true)

        assertTrue(result.isFailure)
        assertIs<DomainException.NetworkException>(result.exceptionOrNull())
    }

    @Test
    fun `getUsers success clears old cache and inserts fresh data`() = runTest {
        val fresh = listOf(userDto(3))
        val api = FakeRemoteUserSource { fresh }
        val local = InMemoryLocalStore(preloaded = listOf(user(1)))
        val repo = UserRepositoryImpl(api, local)

        repo.getUsers()

        assertTrue(local.clearAllCalled)
        assertEquals(1, local.getAllUsers().size)
        assertEquals(3L, local.getAllUsers().first().id)
    }
}
