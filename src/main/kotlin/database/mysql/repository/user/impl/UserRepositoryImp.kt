/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:14 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.repository.user.impl

import com.aatech.data.mysql.model.User
import com.aatech.data.mysql.model.UserTable
import com.aatech.data.mysql.repository.user.UserRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class UserRepositoryImp : UserRepository {
    override suspend fun createUser(user: User) : User {
        val query = UserTable.insert {
            it[uuid] = user.uuid
            it[username] = user.username
            it[fullName] = user.fullName
            it[email] = user.email
            it[profilePicture] = user.profilePicture
            it[passwordHash] = user.passwordHash
            it[createdAt] = user.createdAt
            it[updatedAt] = user.updatedAt
            it[lastLogin] = user.lastLogin
        }
        if (query.insertedCount > 0) {
            return user.copy(uuid = query[UserTable.uuid])
        } else {
            throw Exception("Failed to create User")
        }
    }

    override suspend fun getUserById(userId: String): User? =
        UserTable.selectAll()
            .where { UserTable.uuid eq userId }
            .mapNotNull(::rowToUser).singleOrNull()

}

fun rowToUser(row: ResultRow): User {
    return User(
        uuid = row[UserTable.uuid],
        username = row[UserTable.username],
        fullName = row[UserTable.fullName],
        email = row[UserTable.email],
        profilePicture = row[UserTable.profilePicture],
        passwordHash = row[UserTable.passwordHash],
        createdAt = row[UserTable.createdAt],
        updatedAt = row[UserTable.updatedAt],
        lastLogin = row[UserTable.lastLogin]
    )
}