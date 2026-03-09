package com.sliide.usermanagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliide.usermanagement.data.preferences.DeleteHintStore
import com.sliide.usermanagement.domain.DomainException
import com.sliide.usermanagement.domain.model.User
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

    init {
        loadUsers()
    }

    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getUsersUseCase(forceRefresh)
                .onSuccess { users ->
                    _uiState.update { it.copy(users = users, isLoading = false) }
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

    fun showAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = true, addUserError = null) }
    }

    fun dismissAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = false, addUserError = null) }
    }

    fun createUser(name: String, email: String, gender: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingUser = true, addUserError = null) }
            createUserUseCase(name, email, gender, status)
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
                                is IllegalArgumentException -> CreateUserError.ValidationError(
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
        deleteUserUseCase(pending.user.id).onFailure {
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
            .onSuccess { _uiState.update { it.copy(pendingDeletion = null) } }
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
    }
}
