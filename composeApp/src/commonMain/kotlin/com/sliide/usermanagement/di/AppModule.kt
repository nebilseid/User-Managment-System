package com.sliide.usermanagement.di

import com.sliide.usermanagement.data.local.UserLocalDataSource
import com.sliide.usermanagement.data.local.UserLocalStore
import com.sliide.usermanagement.data.local.db.AppDatabase
import com.sliide.usermanagement.data.preferences.AppPreferences
import com.sliide.usermanagement.data.preferences.DeleteHintStore
import com.sliide.usermanagement.data.remote.RemoteUserSource
import com.sliide.usermanagement.data.remote.UserApi
import com.sliide.usermanagement.data.repository.UserRepositoryImpl
import com.sliide.usermanagement.domain.repository.UserRepository
import com.sliide.usermanagement.domain.usecase.CreateUserUseCase
import com.sliide.usermanagement.domain.usecase.DeleteUserUseCase
import com.sliide.usermanagement.domain.usecase.GetUsersUseCase
import com.sliide.usermanagement.presentation.UsersViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.sliide.usermanagement.isDebugBuild
import com.sliide.usermanagement.supabaseApiKey
import org.koin.dsl.module

val appModule = module {
    // Ktor HttpClient
    single {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = if (isDebugBuild) LogLevel.INFO else LogLevel.NONE
            }
            defaultRequest {
                header("apikey", supabaseApiKey)
                header(HttpHeaders.Authorization, "Bearer $supabaseApiKey")
            }
        }
    }

    // Database
    single { AppDatabase(get<app.cash.sqldelight.db.SqlDriver>()) }
    single { get<com.sliide.usermanagement.data.local.DatabaseDriverFactory>().createDriver() }

    // Data sources
    single<RemoteUserSource> { UserApi(get()) }
    single<UserLocalStore> { UserLocalDataSource(get()) }
    single<DeleteHintStore> { get<AppPreferences>() }

    // Repository
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    // Use cases
    factory { GetUsersUseCase(get()) }
    factory { CreateUserUseCase(get()) }
    factory { DeleteUserUseCase(get()) }

    // ViewModel — registered as single so koinInject() always returns the same instance
    // regardless of composition restarts (avoids IrLinkageError with koin-compose-viewmodel
    // and lifecycle-viewmodel-compose version mismatch on iOS)
    single { UsersViewModel(get(), get(), get(), get()) }
}
