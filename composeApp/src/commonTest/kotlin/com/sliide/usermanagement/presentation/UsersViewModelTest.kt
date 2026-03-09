package com.sliide.usermanagement.presentation

import com.sliide.usermanagement.FakeUserRepository
import com.sliide.usermanagement.data.preferences.DeleteHintStore
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.usecase.CreateUserUseCase
import com.sliide.usermanagement.domain.usecase.DeleteUserUseCase
import com.sliide.usermanagement.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- helpers ---

    private class FakeDeleteHintStore(private var seen: Boolean = true) : DeleteHintStore {
        override fun hasSeenDeleteHint(): Boolean = seen
        override fun markDeleteHintSeen() { seen = true }
    }

    private fun user(id: Long, name: String = "User $id") =
        User(id, name, "user$id@example.com", "male", "active", 0)

    private fun makeViewModel(
        repo: FakeUserRepository = FakeUserRepository(),
        hintStore: DeleteHintStore = FakeDeleteHintStore()
    ) = UsersViewModel(GetUsersUseCase(repo), CreateUserUseCase(repo), DeleteUserUseCase(repo), hintStore)

    // --- load users ---

    @Test
    fun `initial state shows loading and no users`() = runTest(testDispatcher) {
        val repo = FakeUserRepository()
        val vm = makeViewModel(repo)
        // Before any coroutine runs, isLoading should be true (set synchronously in loadUsers)
        assertTrue(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.users.isEmpty())
    }

    @Test
    fun `loadUsers success populates user list and clears loading`() = runTest(testDispatcher) {
        val users = listOf(user(1), user(2))
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(users) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        assertEquals(users, vm.uiState.value.users)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `loadUsers network failure sets NetworkError`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.failure(
                com.sliide.usermanagement.domain.DomainException.NetworkException()
            )
        }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        assertIs<UserError.NetworkError>(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadUsers generic failure sets GenericError`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.failure(RuntimeException("boom"))
        }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        assertIs<UserError.GenericError>(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadUsers clears previous error on retry`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.failure(RuntimeException("boom"))
        }
        val vm = makeViewModel(repo)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)

        repo.getUsersResult = Result.success(listOf(user(1)))
        vm.loadUsers()
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.users.size)
    }

    // --- create user ---

    @Test
    fun `createUser success prepends user to existing list`() = runTest(testDispatcher) {
        val existing = listOf(user(1), user(2))
        val newUser = user(99, "New Person")
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.success(existing)
            it.createUserResult = Result.success(newUser)
        }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.createUser("New Person", "new@example.com", "male", "active")
        advanceUntilIdle()

        assertEquals(newUser, vm.uiState.value.users.first())
        assertEquals(3, vm.uiState.value.users.size)
    }

    @Test
    fun `createUser success closes dialog and clears submitting state`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.success(emptyList())
        }
        val vm = makeViewModel(repo)
        vm.showAddUserDialog()
        advanceUntilIdle()

        vm.createUser("Alice", "alice@example.com", "female", "active")
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showAddUserDialog)
        assertFalse(vm.uiState.value.isSubmittingUser)
        assertNull(vm.uiState.value.addUserError)
    }

    @Test
    fun `createUser failure sets addUserError and keeps dialog open`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.success(emptyList())
            it.createUserResult = Result.failure(RuntimeException("Email already in use"))
        }
        val vm = makeViewModel(repo)
        vm.showAddUserDialog()
        advanceUntilIdle()

        vm.createUser("Alice", "alice@example.com", "female", "active")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.addUserError)
        assertTrue(vm.uiState.value.showAddUserDialog)
        assertFalse(vm.uiState.value.isSubmittingUser)
    }

    // --- input validation ---

    @Test
    fun `createUser with invalid name sets addUserError without calling repository`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(emptyList()) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.createUser("A", "alice@example.com", "female", "active")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.addUserError)
        assertEquals(0, repo.createCallCount)
    }

    @Test
    fun `createUser with invalid email sets addUserError without calling repository`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(emptyList()) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.createUser("Alice", "not-an-email", "female", "active")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.addUserError)
        assertEquals(0, repo.createCallCount)
    }

    @Test
    fun `createUser with blank gender sets addUserError without calling repository`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(emptyList()) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.createUser("Alice", "alice@example.com", "", "active")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.addUserError)
        assertEquals(0, repo.createCallCount)
    }

    @Test
    fun `createUser with blank status sets addUserError without calling repository`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(emptyList()) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.createUser("Alice", "alice@example.com", "female", "")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.addUserError)
        assertEquals(0, repo.createCallCount)
    }

    // --- dialog state ---

    @Test
    fun `showAddUserDialog sets flag and clears stale server error`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.success(emptyList())
            it.createUserResult = Result.failure(RuntimeException("server error"))
        }
        val vm = makeViewModel(repo)
        vm.showAddUserDialog()
        vm.createUser("Alice Smith", "alice@example.com", "female", "active")
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.addUserError)

        vm.showAddUserDialog()

        assertNull(vm.uiState.value.addUserError)
        assertTrue(vm.uiState.value.showAddUserDialog)
    }

    @Test
    fun `dismissAddUserDialog hides dialog and clears error`() = runTest(testDispatcher) {
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.success(emptyList())
            it.createUserResult = Result.failure(RuntimeException("server error"))
        }
        val vm = makeViewModel(repo)
        vm.showAddUserDialog()
        vm.createUser("Alice Smith", "alice@example.com", "female", "active")
        advanceUntilIdle()

        vm.dismissAddUserDialog()

        assertFalse(vm.uiState.value.showAddUserDialog)
        assertNull(vm.uiState.value.addUserError)
    }

    // --- delete / undo ---

    @Test
    fun `initiateDelete removes user optimistically from list`() = runTest(testDispatcher) {
        val users = listOf(user(1), user(2), user(3))
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(users) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(user(2))

        assertEquals(listOf(user(1), user(3)), vm.uiState.value.users)
    }

    @Test
    fun `initiateDelete sets pendingDeletion`() = runTest(testDispatcher) {
        val target = user(2)
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(listOf(target)) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(target)

        assertEquals(target, vm.uiState.value.pendingDeletion?.user)
    }

    @Test
    fun `undoDelete restores user at original index`() = runTest(testDispatcher) {
        val users = listOf(user(1), user(2), user(3))
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(users) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(user(2))
        vm.undoDelete()

        assertEquals(users, vm.uiState.value.users)
    }

    @Test
    fun `undoDelete clears pendingDeletion`() = runTest(testDispatcher) {
        val target = user(1)
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(listOf(target)) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(target)
        vm.undoDelete()

        assertNull(vm.uiState.value.pendingDeletion)
    }

    @Test
    fun `delete is committed to repository after undo timeout`() = runTest(testDispatcher) {
        val target = user(1)
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(listOf(target)) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(target)
        assertTrue(repo.deleteCalledWithIds.isEmpty())

        advanceTimeBy(UsersViewModel.UNDO_TIMEOUT_MS + 1)
        advanceUntilIdle()

        assertEquals(target.id, repo.deleteCalledWithIds.single())
        assertNull(vm.uiState.value.pendingDeletion)
    }

    @Test
    fun `undoDelete before timeout cancels the delete`() = runTest(testDispatcher) {
        val target = user(1)
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(listOf(target)) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(target)
        vm.undoDelete()

        advanceTimeBy(UsersViewModel.UNDO_TIMEOUT_MS + 1)
        advanceUntilIdle()

        assertTrue(repo.deleteCalledWithIds.isEmpty())
    }

    @Test
    fun `delete API failure restores user to original position`() = runTest(testDispatcher) {
        val users = listOf(user(1), user(2))
        val repo = FakeUserRepository().also {
            it.getUsersResult = Result.success(users)
            it.deleteUserResult = Result.failure(RuntimeException("server error"))
        }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(user(1))
        advanceTimeBy(UsersViewModel.UNDO_TIMEOUT_MS + 1)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.users.any { it.id == 1L })
    }

    @Test
    fun `second initiateDelete flushes first and commits second`() = runTest(testDispatcher) {
        val users = listOf(user(1), user(2))
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(users) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.initiateDelete(user(1))
        vm.initiateDelete(user(2))

        // Only the second delete should be pending (first was displaced and flushed)
        assertEquals(user(2), vm.uiState.value.pendingDeletion?.user)

        // Advance past timeout — first delete flushed immediately, second commits after timeout
        advanceTimeBy(UsersViewModel.UNDO_TIMEOUT_MS + 1)
        advanceUntilIdle()

        assertEquals(setOf(1L, 2L), repo.deleteCalledWithIds.toSet())
        assertNull(vm.uiState.value.pendingDeletion)
    }

    // --- master-detail ---

    @Test
    fun `selectUser updates selectedUser in state`() = runTest(testDispatcher) {
        val target = user(1)
        val repo = FakeUserRepository().also { it.getUsersResult = Result.success(listOf(target)) }
        val vm = makeViewModel(repo)
        advanceUntilIdle()

        vm.selectUser(target)
        assertEquals(target, vm.uiState.value.selectedUser)

        vm.selectUser(null)
        assertNull(vm.uiState.value.selectedUser)
    }
}
