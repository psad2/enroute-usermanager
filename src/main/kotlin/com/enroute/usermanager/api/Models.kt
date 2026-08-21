package com.enroute.usermanager.api

import kotlinx.serialization.Serializable

// These mirror the backend's JSON shapes exactly (backend/src/main/kotlin/
// user-admin.kt and session-managment.kt). The backend serializes with
// SnakeCase naming, so every field here uses camelCase and relies on
// Ktor's ContentNegotiation to do the snake_case <-> camelCase mapping.

@Serializable
data class AdminUserView(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val banned: Boolean,
    val timeoutUntil: String?,
    val joinedAt: String?
)

@Serializable
data class AdminUserListResponse(
    val users: List<AdminUserView>,
    val page: Int,
    val perPage: Int,
    val total: Int
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val message: String,
    val token: String,
    val user: PublicUser
)

@Serializable
data class PublicUser(
    val id: Long,
    val username: String,
    val email: String
)

@Serializable
data class RoleChangeRequest(val role: String)

@Serializable
data class TimeoutRequest(val minutes: Int)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class ErrorResponse(val error: String)

enum class UserRole {
    USER, MODERATOR, ADMIN;

    val label: String
        get() = when (this) {
            USER -> "User"
            MODERATOR -> "Moderator"
            ADMIN -> "Admin"
        }

    val apiValue: String
        get() = name.lowercase()

    companion object {
        fun fromApiValue(value: String): UserRole =
            entries.firstOrNull { it.apiValue == value.lowercase() } ?: USER
    }
}
