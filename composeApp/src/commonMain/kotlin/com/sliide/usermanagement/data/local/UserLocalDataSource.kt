package com.sliide.usermanagement.data.local

import com.sliide.usermanagement.data.local.db.AppDatabase
import com.sliide.usermanagement.domain.model.User

class UserLocalDataSource(private val database: AppDatabase) : UserLocalStore {

    override fun getAllUsers(): List<User> =
        database.appDatabaseQueries.selectAll().executeAsList().map { entity ->
            User(
                id = entity.id,
                name = entity.name,
                email = entity.email,
                gender = entity.gender,
                status = entity.status,
                createdAt = entity.createdAt
            )
        }

    override fun insertUsers(users: List<User>) {
        database.appDatabaseQueries.transaction {
            users.forEach { user ->
                database.appDatabaseQueries.insert(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    gender = user.gender,
                    status = user.status,
                    createdAt = user.createdAt
                )
            }
        }
    }

    override fun insertUser(user: User) {
        database.appDatabaseQueries.insert(
            id = user.id,
            name = user.name,
            email = user.email,
            gender = user.gender,
            status = user.status,
            createdAt = user.createdAt
        )
    }

    override fun deleteUser(id: Long) {
        database.appDatabaseQueries.deleteById(id)
    }

    override fun clearAll() {
        database.appDatabaseQueries.deleteAll()
    }
}
