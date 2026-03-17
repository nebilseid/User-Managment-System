package com.sliide.usermanagement.data.remote

import com.sliide.usermanagement.data.remote.dto.CreateUserRequest
import com.sliide.usermanagement.data.remote.dto.UserDto
import com.sliide.usermanagement.data.remote.dto.UsersResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserApi(private val client: HttpClient) : RemoteUserSource {

    override suspend fun fetchUsers(skip: Int, limit: Int): UsersResponse {
        return client.get("$BASE_URL/users") {
            parameter("limit", limit)
            parameter("skip", skip)
        }.body<UsersResponse>()
    }

    override suspend fun createUser(name: String, email: String, gender: String, status: String): UserDto {
        return client.post("$BASE_URL/users/add") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(firstName = name, email = email, gender = gender, status = status))
        }.body()
    }

    override suspend fun deleteUser(id: Long) {
        try {
            client.delete("$BASE_URL/users/$id")
        } catch (e: ResponseException) {
            // 404 means the resource never existed on the server (e.g. locally-created user).
            // Treat as success — the delete intent is already satisfied.
            if (e.response.status.value != 404) throw e
        }
    }

    companion object {
        const val BASE_URL = "https://dummyjson.com"
    }
}
