package com.enroute.usermanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enroute.usermanager.api.AdminApiClient
import com.enroute.usermanager.api.AdminUserView
import com.enroute.usermanager.api.ApiResult
import com.enroute.usermanager.api.UserRole
import com.enroute.usermanager.theme.EnrouteColors
import kotlinx.coroutines.launch

@Composable
fun UserManagerScreen(
    client: AdminApiClient,
    onLogOut: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<AdminUserView>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        isLoading = true
        errorMessage = null
        scope.launch {
            when (val result = client.listUsers(query = query.ifBlank { null })) {
                is ApiResult.Success -> {
                    users = result.value.users
                    isLoading = false
                }
                is ApiResult.Failure -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "User Manager",
                style = MaterialTheme.typography.headlineSmall,
                color = EnrouteColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { client.logOut(); onLogOut() }) {
                Text("Log out", color = EnrouteColors.TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search username or email") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { refresh() }
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                )
            )
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = { refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EnrouteColors.AccentLight)
            }
        }

        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = EnrouteColors.Success, style = MaterialTheme.typography.bodySmall)
        }
        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = EnrouteColors.Danger, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading && users.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (users.isEmpty()) {
                Text(
                    "No users found",
                    color = EnrouteColors.TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users, key = { it.id }) { user ->
                        UserRow(
                            user = user,
                            onChangeRole = { role ->
                                pendingAction = PendingAction.ChangeRole(user, role)
                            },
                            onTimeout = { pendingAction = PendingAction.Timeout(user) },
                            onClearTimeout = { pendingAction = PendingAction.ClearTimeout(user) },
                            onBanToggle = {
                                pendingAction = if (user.banned) {
                                    PendingAction.Unban(user)
                                } else {
                                    PendingAction.Ban(user)
                                }
                            },
                            onDelete = { pendingAction = PendingAction.Delete(user) }
                        )
                    }
                }
            }
        }
    }

    pendingAction?.let { action ->
        ActionConfirmDialog(
            action = action,
            onDismiss = { pendingAction = null },
            onConfirm = { minutes ->
                pendingAction = null
                statusMessage = null
                errorMessage = null

                scope.launch {
                    val result = when (action) {
                        is PendingAction.ChangeRole -> client.changeRole(action.user.id, action.role)
                        is PendingAction.Timeout -> client.timeoutUser(action.user.id, minutes ?: 60)
                        is PendingAction.ClearTimeout -> client.clearTimeout(action.user.id)
                        is PendingAction.Ban -> client.banUser(action.user.id)
                        is PendingAction.Unban -> client.unbanUser(action.user.id)
                        is PendingAction.Delete -> client.deleteUser(action.user.id)
                    }

                    when (result) {
                        is ApiResult.Success -> {
                            statusMessage = result.value.message
                            refresh()
                        }
                        is ApiResult.Failure -> errorMessage = result.message
                    }
                }
            }
        )
    }
}

@Composable
private fun UserRow(
    user: AdminUserView,
    onChangeRole: (UserRole) -> Unit,
    onTimeout: () -> Unit,
    onClearTimeout: () -> Unit,
    onBanToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = EnrouteColors.Surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = EnrouteColors.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip(user)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${user.email} · #${user.id} · joined ${user.joinedAt ?: "unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EnrouteColors.TextSecondary
                )
            }

            RoleDropdown(current = UserRole.fromApiValue(user.role), onSelect = onChangeRole)

            Spacer(Modifier.width(8.dp))

            if (user.timeoutUntil != null) {
                TextButton(onClick = onClearTimeout) {
                    Text("Clear timeout", color = EnrouteColors.Warning)
                }
            } else {
                TextButton(onClick = onTimeout) {
                    Text("Timeout", color = EnrouteColors.TextSecondary)
                }
            }

            TextButton(onClick = onBanToggle) {
                Text(if (user.banned) "Unban" else "Ban", color = EnrouteColors.Warning)
            }

            TextButton(onClick = onDelete) {
                Text("Delete", color = EnrouteColors.Danger)
            }
        }
    }
}

@Composable
private fun StatusChip(user: AdminUserView) {
    val (label, color) = when {
        user.banned -> "Banned" to EnrouteColors.Danger
        user.timeoutUntil != null -> "Timed out" to EnrouteColors.Warning
        else -> "Active" to EnrouteColors.Success
    }
    Surface(color = color.copy(alpha = 0.15f)) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RoleDropdown(current: UserRole, onSelect: (UserRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(current.label, color = EnrouteColors.AccentLight, fontWeight = FontWeight.Medium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.label) },
                    onClick = {
                        expanded = false
                        if (role != current) onSelect(role)
                    }
                )
            }
        }
    }
}

private sealed class PendingAction {
    abstract val user: AdminUserView

    data class ChangeRole(override val user: AdminUserView, val role: UserRole) : PendingAction()
    data class Timeout(override val user: AdminUserView) : PendingAction()
    data class ClearTimeout(override val user: AdminUserView) : PendingAction()
    data class Ban(override val user: AdminUserView) : PendingAction()
    data class Unban(override val user: AdminUserView) : PendingAction()
    data class Delete(override val user: AdminUserView) : PendingAction()
}

@Composable
private fun ActionConfirmDialog(
    action: PendingAction,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int?) -> Unit
) {
    var minutesText by remember { mutableStateOf("60") }

    val (title, body, confirmLabel, confirmColor) = when (action) {
        is PendingAction.ChangeRole -> DialogCopy(
            "Change role",
            "Set ${action.user.username}'s role to ${action.role.label}?",
            "Confirm", EnrouteColors.AccentPrimary
        )
        is PendingAction.Timeout -> DialogCopy(
            "Time out user",
            "Temporarily suspend ${action.user.username} and log them out immediately.",
            "Time out", EnrouteColors.Warning
        )
        is PendingAction.ClearTimeout -> DialogCopy(
            "Clear timeout",
            "Restore ${action.user.username}'s access immediately.",
            "Clear timeout", EnrouteColors.AccentPrimary
        )
        is PendingAction.Ban -> DialogCopy(
            "Ban user",
            "Permanently ban ${action.user.username}. They will be logged out immediately and unable to sign back in until unbanned.",
            "Ban", EnrouteColors.Danger
        )
        is PendingAction.Unban -> DialogCopy(
            "Unban user",
            "Restore ${action.user.username}'s ability to sign in.",
            "Unban", EnrouteColors.AccentPrimary
        )
        is PendingAction.Delete -> DialogCopy(
            "Delete user",
            "Permanently delete ${action.user.username} and everything they authored. This cannot be undone.",
            "Delete", EnrouteColors.Danger
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(body)
                if (action is PendingAction.Timeout) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter(Char::isDigit) },
                        label = { Text("Duration (minutes)") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(minutesText.toIntOrNull()) },
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private data class DialogCopy(
    val title: String,
    val body: String,
    val confirmLabel: String,
    val confirmColor: androidx.compose.ui.graphics.Color
)
