package com.sliide.usermanagement.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sliide.usermanagement.domain.model.User
import com.sliide.usermanagement.presentation.UserError
import com.sliide.usermanagement.presentation.UsersViewModel
import com.sliide.usermanagement.ui.components.AddUserDialog
import com.sliide.usermanagement.ui.components.DeleteConfirmDialog
import com.sliide.usermanagement.ui.components.DeleteSnackbar
import com.sliide.usermanagement.ui.components.ErrorBlock
import com.sliide.usermanagement.ui.components.ErrorSnackbar
import com.sliide.usermanagement.ui.components.ErrorSnackbarVisuals
import com.sliide.usermanagement.ui.components.ShimmerUserCard
import com.sliide.usermanagement.ui.components.UserCard
import com.sliide.usermanagement.ui.components.UserDetailPanel
import com.sliide.usermanagement.ui.strings.AppStrings
import com.sliide.usermanagement.ui.util.toTitleCase
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val windowInfo = LocalWindowInfo.current
    val isExpanded = with(LocalDensity.current) { windowInfo.containerSize.width.toDp() } > 600.dp
    val pullRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    var wasSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSubmittingUser, uiState.showAddUserDialog) {
        if (wasSubmitting && !uiState.isSubmittingUser && !uiState.showAddUserDialog) {
            listState.animateScrollToItem(0)
        }
        wasSubmitting = uiState.isSubmittingUser
    }

    LaunchedEffect(uiState.pendingDeletion) {
        uiState.pendingDeletion?.let { pending ->
            val result = snackbarHostState.showSnackbar(
                message = AppStrings.deletedMessage(pending.user.name.toTitleCase()),
                actionLabel = AppStrings.SNACKBAR_UNDO,
                duration = SnackbarDuration.Long
            )
            if (result   == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    LaunchedEffect(uiState.error) {
        val error = uiState.error
        if (error != null && uiState.users.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                ErrorSnackbarVisuals(
                    message = when (error) {
                        is UserError.NetworkError -> AppStrings.ERROR_NO_INTERNET_SNACKBAR
                        is UserError.GenericError -> AppStrings.ERROR_SERVER_SNACKBAR
                    },
                    isNetworkError = error is UserError.NetworkError
                )
            )
        }
    }

    // Trigger load-more when within 5 items of the end of the list
    val shouldLoadMore by remember {
        derivedStateOf {
            val listInfo = listState.layoutInfo
            listInfo.totalItemsCount > 0 &&
                    (listInfo.visibleItemsInfo.lastOrNull()?.index ?: -1) >= listInfo.totalItemsCount - 5
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoadingMore && uiState.hasMore && uiState.users.isNotEmpty()) {
            viewModel.loadMoreUsers()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fabExpanded by remember { derivedStateOf { scrollBehavior.state.collapsedFraction == 0f } }

    // On mobile (non-expanded), navigate to a full-screen detail when a user is selected.
    // On tablet (expanded), the TwoColumnLayout handles master-detail inline.
    val showDetail = !isExpanded && uiState.selectedUser != null

    // Retain the last selected user so the exit animation has content to slide out
    var stagedUser by remember { mutableStateOf(uiState.selectedUser) }
    if (uiState.selectedUser != null) stagedUser = uiState.selectedUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        UsersListScaffold(
            uiState = uiState,
            viewModel = viewModel,
            isExpanded = isExpanded,
            snackbarHostState = snackbarHostState,
            pullRefreshState = pullRefreshState,
            listState = listState,
            scrollBehavior = scrollBehavior,
            fabExpanded = fabExpanded
        )

        AnimatedVisibility(
            visible = showDetail,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            stagedUser?.let { user ->
                UserDetailScreen(
                    user = user,
                    onBack = { viewModel.selectUser(null) }
                )
            }
        }
    }

    uiState.confirmingDeleteUser?.let { user ->
        DeleteConfirmDialog(
            user = user,
            onDismiss = { viewModel.dismissDeleteConfirmation() },
            onConfirm = { viewModel.initiateDelete(user) }
        )
    }

    if (uiState.showAddUserDialog) {
        AddUserDialog(
            isSubmitting = uiState.isSubmittingUser,
            serverError = uiState.addUserError,
            onDismiss = { viewModel.dismissAddUserDialog() },
            onConfirm = { name, email, gender, status ->
                viewModel.createUser(name, email, gender, status)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsersListScaffold(
    uiState: com.sliide.usermanagement.presentation.UsersUiState,
    viewModel: UsersViewModel,
    isExpanded: Boolean,
    snackbarHostState: SnackbarHostState,
    pullRefreshState: androidx.compose.material3.pulltorefresh.PullToRefreshState,
    listState: LazyListState,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    fabExpanded: Boolean
) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = AppStrings.USERS_TITLE,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    AnimatedVisibility(
                        visible = uiState.showDeleteHint && uiState.users.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = AppStrings.HINT_LONG_PRESS_DELETE,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddUserDialog() },
                expanded = fabExpanded,
                icon = { Icon(Icons.Default.Add, contentDescription = AppStrings.FAB_ADD_USER_CD) },
                text = { Text(AppStrings.FAB_ADD_USER) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                if (data.visuals is ErrorSnackbarVisuals) ErrorSnackbar(data) else DeleteSnackbar(data)
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = uiState.isLoading && uiState.users.isNotEmpty(),
            onRefresh = { viewModel.loadUsers(forceRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = uiState.isLoading && uiState.users.isNotEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            when {
                uiState.isLoading && uiState.users.isEmpty() -> LoadingContent(isExpanded)
                uiState.error != null && uiState.users.isEmpty() -> ErrorBlock(
                    title = when (uiState.error) {
                        is UserError.NetworkError -> AppStrings.ERROR_NO_INTERNET_TITLE
                        is UserError.GenericError -> AppStrings.ERROR_SERVER_TITLE
                        null -> ""
                    },
                    message = when (val error = uiState.error) {
                        is UserError.NetworkError -> AppStrings.ERROR_NO_INTERNET_MSG
                        is UserError.GenericError -> error.message ?: AppStrings.ERROR_GENERIC_FALLBACK
                        null -> ""
                    },
                    onRetry = { viewModel.loadUsers() }
                )
                uiState.users.isEmpty() -> EmptyContent()
                isExpanded -> TwoColumnLayout(
                    uiState.users,
                    uiState.selectedUser,
                    viewModel::selectUser,
                    onLongClick = { viewModel.requestDeleteConfirmation(it) },
                    listState = listState,
                    isLoadingMore = uiState.isLoadingMore
                )
                else -> SingleColumnLayout(
                    uiState.users,
                    onClick = { viewModel.selectUser(it) },
                    onLongClick = { viewModel.requestDeleteConfirmation(it) },
                    listState = listState,
                    isLoadingMore = uiState.isLoadingMore
                )
            }
        }
    }
}

@Composable
private fun SingleColumnLayout(
    users: List<User>,
    onClick: (User) -> Unit,
    onLongClick: (User) -> Unit,
    listState: LazyListState,
    isLoadingMore: Boolean = false
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(users, key = { _, user -> user.id }) { _, user ->
            UserCard(
                user = user,
                onClick = { onClick(user) },
                onLongClick = { onLongClick(user) }
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoColumnLayout(
    users: List<User>,
    selectedUser: User?,
    onSelect: (User?) -> Unit,
    onLongClick: (User) -> Unit,
    listState: LazyListState,
    isLoadingMore: Boolean = false
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(users, key = { _, user -> user.id }) { _, user ->
                UserCard(
                    user = user,
                    isSelected = user.id == selectedUser?.id,
                    onClick = { onSelect(user) },
                    onLongClick = { onLongClick(user) }
                )
            }
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (selectedUser != null) {
            UserDetailPanel(
                user = selectedUser,
                modifier = Modifier.weight(1f)
            )
        } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    AppStrings.DETAIL_PLACEHOLDER,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(isExpanded: Boolean) {
    if (isExpanded) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(8) { ShimmerUserCard() }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(8) { ShimmerUserCard() }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                AppStrings.EMPTY_TITLE,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                AppStrings.EMPTY_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 40.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
