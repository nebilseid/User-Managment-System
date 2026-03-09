package com.sliide.usermanagement.presentation

import com.sliide.usermanagement.domain.model.User

data class PendingDeletion(val user: User, val index: Int)

data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: UserError? = null,
    val showAddUserDialog: Boolean = false,
    val isSubmittingUser: Boolean = false,
    val addUserError: CreateUserError? = null,
    val pendingDeletion: PendingDeletion? = null,
    val confirmingDeleteUser: User? = null,
    val selectedUser: User? = null, // for master-detail layout
    val showDeleteHint: Boolean = false
)

sealed class UserError {
    data object NetworkError : UserError()
    data class GenericError(val message: String) : UserError()
}

sealed class CreateUserError {
    data object NetworkError : CreateUserError()
    data object DuplicateEmail : CreateUserError()
    data object ServerError : CreateUserError()
    data class ValidationError(val message: String) : CreateUserError()
    data object Unknown : CreateUserError()
}
