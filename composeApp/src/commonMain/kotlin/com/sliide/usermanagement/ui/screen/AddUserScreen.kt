package com.sliide.usermanagement.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sliide.usermanagement.domain.util.isValidEmail
import com.sliide.usermanagement.domain.util.isValidName
import com.sliide.usermanagement.presentation.CreateUserError
import com.sliide.usermanagement.ui.components.ChipGroup
import com.sliide.usermanagement.ui.components.chipColors
import com.sliide.usermanagement.ui.strings.AppStrings
import com.sliide.usermanagement.ui.util.PlatformBackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    isSubmitting: Boolean,
    serverError: CreateUserError?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, gender: String, status: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var nameFocusedOnce by remember { mutableStateOf(false) }
    var emailFocusedOnce by remember { mutableStateOf(false) }
    var nameDirty by remember { mutableStateOf(false) }
    var emailDirty by remember { mutableStateOf(false) }

    val nameError = if (nameDirty && !isValidName(name)) AppStrings.ERROR_INVALID_NAME else null
    val emailError = if (emailDirty && !isValidEmail(email)) AppStrings.ERROR_INVALID_EMAIL else null

    val nameEmailComplete = isValidName(name) && isValidEmail(email)
    val needsGender = nameEmailComplete && gender.isEmpty()
    val needsStatus = nameEmailComplete && gender.isNotEmpty() && status.isEmpty()
    val isFormValid = nameEmailComplete && gender.isNotEmpty() && status.isNotEmpty()

    val focusManager = LocalFocusManager.current

    PlatformBackHandler(onBack = { if (!isSubmitting) onDismiss() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.DIALOG_ADD_TITLE,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (!isSubmitting) onDismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.CD_CLOSE
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Button(
                    onClick = { onConfirm(name.trim(), email.trim(), gender, status) },
                    enabled = isFormValid && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            AppStrings.BTN_CREATE_USER,
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(AppStrings.FIELD_FULL_NAME) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) nameFocusedOnce = true
                        else if (nameFocusedOnce) nameDirty = true
                    }
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(AppStrings.FIELD_EMAIL) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) emailFocusedOnce = true
                        else if (emailFocusedOnce) emailDirty = true
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ChipGroup(
                label = AppStrings.LABEL_GENDER_SECTION,
                options = listOf("male", "female"),
                selected = gender,
                highlighted = needsGender,
                onSelect = { gender = it }
            )
            Spacer(modifier = Modifier.height(8.dp))

            ChipGroup(
                label = AppStrings.LABEL_STATUS_SECTION,
                options = listOf("active", "inactive"),
                selected = status,
                highlighted = needsStatus,
                onSelect = { status = it }
            )

            AnimatedVisibility(
                visible = serverError != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (serverError) {
                                is CreateUserError.NetworkError -> AppStrings.ERROR_ADD_NO_INTERNET
                                is CreateUserError.DuplicateEmail -> AppStrings.ERROR_ADD_DUPLICATE_EMAIL
                                is CreateUserError.ServerError -> AppStrings.ERROR_ADD_SERVER
                                is CreateUserError.ValidationError -> serverError.message ?: AppStrings.ERROR_GENERIC_FALLBACK
                                is CreateUserError.Unknown, null -> AppStrings.ERROR_ADD_UNKNOWN
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
