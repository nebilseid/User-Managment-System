package com.sliide.usermanagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliide.usermanagement.data.preferences.DeleteHintStore
import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.CreateUserRequest
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.domain.repository.UserRepository
import com.sliide.usermanagement.domain.usecase.CreateUserUseCase
import com.sliide.usermanagement.domain.usecase.DeleteUserUseCase
import com.sliide.usermanagement.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val deleteHintStore: DeleteHintStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UsersUiState(isLoading = true, showDeleteHint = !deleteHintStore.hasSeenDeleteHint())
    )
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private var deletionJob: Job? = null
    // Ids committed to delete this session. DummyJSON doesn't persist deletes, so we track
    // them locally and filter them out of every server response for the lifetime of the app.
    private val deletedIds = mutableSetOf<Long>()

    init {
        loadUsers()
    }

    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentSkip = 0, hasMore = true) }
            getUsersUseCase(forceRefresh, skip = 0, limit = PAGE_SIZE)
                .onSuccess { users ->
                    _uiState.update { state ->
                        // Keep the pending-deleted user out of the refreshed list so it doesn't
                        // reappear mid-undo-window. Pagination offsets are still derived from the
                        // raw server count so skip/limit stay in sync.
                        val excludeIds = deletedIds + setOfNotNull(state.pendingDeletion?.user?.id)
                        val visible = if (excludeIds.isEmpty()) users
                            else users.filter { it.id !in excludeIds }
                        state.copy(
                            users = visible,
                            isLoading = false,
                            hasMore = users.size >= PAGE_SIZE,
                            currentSkip = users.size
                        )
                    }
                    scheduleDeleteHintDismissal()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = when (error) {
                                is DomainException.NetworkException -> UserError.NetworkError
                                else -> UserError.GenericError("Server error, please try again")
                            }
                        )
                    }
                }
        }
    }

    fun loadMoreUsers() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            getUsersUseCase(skip = state.currentSkip, limit = PAGE_SIZE)
                .onSuccess { newUsers ->
                    _uiState.update {
                        it.copy(
                            users = it.users + newUsers,
                            isLoadingMore = false,
                            hasMore = newUsers.size >= PAGE_SIZE,
                            currentSkip = it.currentSkip + newUsers.size
                        )
                    }
                }
                .onFailure {
                    // Silently clear the loading state; the user can scroll to retry
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun showAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = true, addUserError = null) }
    }

    fun dismissAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = false, addUserError = null) }
    }

    fun createUser(name: String, email: String, gender: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingUser = true, addUserError = null) }
            createUserUseCase(CreateUserRequest(name = name, email = email, gender = gender, status = status))
                .onSuccess { user ->
                    _uiState.update { state ->
                        state.copy(
                            users = listOf(user) + state.users,
                            showAddUserDialog = false,
                            isSubmittingUser = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmittingUser = false,
                            addUserError = when (error) {
                                is DomainException.NetworkException -> CreateUserError.NetworkError
                                is DomainException.ConflictException -> CreateUserError.DuplicateEmail
                                is DomainException.ServerException -> CreateUserError.ServerError
                                is DomainException.ValidationException -> CreateUserError.ValidationError(
                                    error.message ?: "Invalid input"
                                )
                                else -> CreateUserError.Unknown
                            }
                        )
                    }
                }
        }
    }

    fun requestDeleteConfirmation(user: User) {
        _uiState.update { it.copy(confirmingDeleteUser = user) }
        dismissDeleteHint()
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(confirmingDeleteUser = null) }
    }

    fun initiateDelete(user: User) {
        val index = _uiState.value.users.indexOf(user)

        // If another delete is pending, flush it to the API immediately before replacing it.
        // Cancelling the old job without committing would silently drop that delete.
        val displaced = _uiState.value.pendingDeletion
        deletionJob?.cancel()
        if (displaced != null) {
            viewModelScope.launch { flushDelete(displaced) }
        }

        _uiState.update { state ->
            state.copy(
                users = state.users.filter { it.id != user.id },
                pendingDeletion = PendingDeletion(user, index),
                confirmingDeleteUser = null,
                selectedUser = if (state.selectedUser?.id == user.id) null else state.selectedUser
            )
        }

        deletionJob = viewModelScope.launch {
            delay(UNDO_TIMEOUT_MS)
            commitDelete(user.id)
        }
    }

    // Commits a displaced pending deletion (no undo window). On failure, restores
    // the user directly using the captured PendingDeletion rather than reading state.
    private suspend fun flushDelete(pending: PendingDeletion) {
        deleteUserUseCase(pending.user.id)
            .onSuccess { deletedIds.add(pending.user.id) }
            .onFailure {
                _uiState.update { state ->
                    val mutableList = state.users.toMutableList()
                    mutableList.add(pending.index.coerceIn(0, mutableList.size), pending.user)
                    state.copy(users = mutableList)
                }
            }
    }

    fun undoDelete() {
        deletionJob?.cancel()
        deletionJob = null
        restoreDeletedUser()
    }

    fun selectUser(user: User?) {
        _uiState.update { it.copy(selectedUser = user) }
    }

    fun dismissDeleteHint() {
        if (!_uiState.value.showDeleteHint) return
        deleteHintStore.markDeleteHintSeen()
        _uiState.update { it.copy(showDeleteHint = false) }
    }

    private fun scheduleDeleteHintDismissal() {
        if (!_uiState.value.showDeleteHint) return
        viewModelScope.launch {
            delay(HINT_DISPLAY_MS)
            dismissDeleteHint()
        }
    }

    private suspend fun commitDelete(id: Long) {
        deleteUserUseCase(id)
            .onSuccess {
                deletedIds.add(id)
                _uiState.update { it.copy(pendingDeletion = null) }
            }
            .onFailure { restoreDeletedUser() }
        deletionJob = null
    }

    private fun restoreDeletedUser() {
        val pending = _uiState.value.pendingDeletion ?: return
        _uiState.update { state ->
            val mutableList = state.users.toMutableList()
            val insertIndex = pending.index.coerceIn(0, mutableList.size)
            mutableList.add(insertIndex, pending.user)
            state.copy(users = mutableList, pendingDeletion = null)
        }
    }

    companion object {
        const val UNDO_TIMEOUT_MS = 4_000L
        const val HINT_DISPLAY_MS = 3_500L
        const val PAGE_SIZE = UserRepository.PAGE_SIZE
    }
}
