package com.sliide.usermanagement.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sliide.usermanagement.domain.util.isValidEmail
import com.sliide.usermanagement.domain.util.isValidName
import com.sliide.usermanagement.presentation.CreateUserError

@Composable
fun AddUserDialog(
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

    val nameError = if (nameDirty && !isValidName(name)) "Enter a valid full name" else null
    val emailError = if (emailDirty && !isValidEmail(email)) "Enter a valid email address" else null

    val nameEmailComplete = isValidName(name) && isValidEmail(email)
    val needsGender = nameEmailComplete && gender.isEmpty()
    val needsStatus = nameEmailComplete && gender.isNotEmpty() && status.isEmpty()
    val isFormValid = nameEmailComplete && gender.isNotEmpty() && status.isNotEmpty()

    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 0.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "New User",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
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
                    label = { Text("Email") },
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
                    label = "Gender",
                    options = listOf("male", "female"),
                    selected = gender,
                    highlighted = needsGender,
                    onSelect = { gender = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                ChipGroup(
                    label = "Status",
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
                                    is CreateUserError.NetworkError ->
                                        "No internet connection. Check your network and try again."
                                    is CreateUserError.DuplicateEmail ->
                                        "This email is already registered. Please use a different one."
                                    is CreateUserError.ServerError ->
                                        "Server error. Please try again later."
                                    is CreateUserError.ValidationError -> serverError.message
                                    is CreateUserError.Unknown, null ->
                                        "Something went wrong. Please try again."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirm(name.trim(), email.trim(), gender, status) },
                    enabled = isFormValid && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
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
                            "CREATE USER",
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipGroup(
    label: String,
    options: List<String>,
    selected: String,
    highlighted: Boolean,
    onSelect: (String) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val borderColor by animateColorAsState(
        targetValue = if (highlighted) primary.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "chipGroupBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (highlighted) primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(option.replaceFirstChar { it.uppercase() }) },
                    colors = chipColors()
                )
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
)
