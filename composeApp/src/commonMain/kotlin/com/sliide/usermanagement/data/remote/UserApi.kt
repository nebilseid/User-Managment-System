package com.sliide.usermanagement.data.remote

import com.sliide.usermanagement.data.remote.dto.CreateUserRequest
import com.sliide.usermanagement.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserApi(private val client: HttpClient) : RemoteUserSource {

    override suspend fun fetchUsers(): List<UserDto> {
        return client.get("$BASE_URL/users") {
            parameter("order", "id.desc")
        }.body()
    }

    override suspend fun createUser(name: String, email: String, gender: String, status: String): UserDto {
        return client.post("$BASE_URL/users") {
            contentType(ContentType.Application.Json)
            header("Prefer", "return=representation")
            setBody(CreateUserRequest(name, email, gender, status))
        }.body<List<UserDto>>().firstOrNull()
            ?: throw IllegalStateException("Server returned empty response for created user")
    }

    override suspend fun deleteUser(id: Long) {
        client.delete("$BASE_URL/users") {
            parameter("id", "eq.$id")
        }
    }

    companion object {
        const val BASE_URL = "https://oxcynzvwuywnjwwhjdms.supabase.co/rest/v1"
    }
}
