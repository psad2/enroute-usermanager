package com.enroute.usermanager.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// A small sealed result type so the UI layer never has to deal with
// exceptions directly - every call either succeeds with a value, or fails
// with a message that is safe to show the user.
sealed class ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>()
    data class Failure(val message: String) : ApiResult<Nothing>()
}

// Talks to the same Ktor backend the website uses (backend/src/main/kotlin).
// baseUrl points at wherever that backend is deployed, e.g.
// https://forum.enroute-example.com or an SSH-tunnelled http://127.0.0.1:5000.
class AdminApiClient(
    private var baseUrl: String
) {
    private var authToken: String? = null

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
    }

    fun updateBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun isLoggedIn(): Boolean = authToken != null

    fun logOut() {
        authToken = null
    }

    private fun url(path: String) = "$baseUrl$path"

    private suspend inline fun <reified T> parse(response: HttpResponse): ApiResult<T> {
        if (response.status.isSuccess()) {
            return ApiResult.Success(response.body())
        }

        val error = try {
            response.body<ErrorResponse>().error
        } catch (_: Exception) {
            "Request failed (${response.status.value})"
        }

        return ApiResult.Failure(error)
    }

    suspend fun login(username: String, password: String): ApiResult<PublicUser> {
        return try {
            val response = client.post(url("/api/login")) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (!response.status.isSuccess()) {
                return parse(response)
            }

            val body = response.body<LoginResponse>()
            authToken = body.token
            ApiResult.Success(body.user)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun listUsers(
        query: String? = null,
        page: Int = 1,
        perPage: Int = 50
    ): ApiResult<AdminUserListResponse> {
        return try {
            val response = client.get(url("/api/admin/users")) {
                bearerAuth()
                parameter("page", page)
                parameter("per_page", perPage)
                if (!query.isNullOrBlank()) parameter("query", query)
            }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun changeRole(userId: Long, role: UserRole): ApiResult<MessageResponse> {
        return try {
            val response = client.patch(url("/api/admin/users/$userId/role")) {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(RoleChangeRequest(role.apiValue))
            }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun timeoutUser(userId: Long, minutes: Int): ApiResult<MessageResponse> {
        return try {
            val response = client.post(url("/api/admin/users/$userId/timeout")) {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(TimeoutRequest(minutes))
            }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun clearTimeout(userId: Long): ApiResult<MessageResponse> {
        return try {
            val response = client.post(url("/api/admin/users/$userId/untimeout")) { bearerAuth() }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun banUser(userId: Long): ApiResult<MessageResponse> {
        return try {
            val response = client.post(url("/api/admin/users/$userId/ban")) { bearerAuth() }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun unbanUser(userId: Long): ApiResult<MessageResponse> {
        return try {
            val response = client.post(url("/api/admin/users/$userId/unban")) { bearerAuth() }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    suspend fun deleteUser(userId: Long): ApiResult<MessageResponse> {
        return try {
            val response = client.delete(url("/api/admin/users/$userId")) { bearerAuth() }
            parse(response)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Could not reach the server")
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerAuth() {
        authToken?.let { header("Authorization", "Bearer $it") }
    }
}
