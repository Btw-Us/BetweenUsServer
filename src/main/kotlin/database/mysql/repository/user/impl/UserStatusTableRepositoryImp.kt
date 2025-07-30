/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:28 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.user.impl

import com.aatech.database.mysql.mapper.rowToUserStatus
import com.aatech.database.mysql.model.UserStatusTable
import com.aatech.database.mysql.model.entity.UserStatus
import com.aatech.database.mysql.repository.user.UserStatusRepository
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class UserStatusTableRepositoryImp : UserStatusRepository {
    override suspend fun createUser(userStatus: UserStatus): UserStatus {
        val query = UserStatusTable.insert {
            it[userId] = userStatus.userId
            it[status] = userStatus.status
            it[lastUpdated] = userStatus.lastUpdated
        }
        if (query.insertedCount > 0) {
            return userStatus.copy(userId = query[UserStatusTable.userId])
        } else {
            throw Exception("Failed to create User Status")
        }
    }

    override suspend fun getUserById(userId: String): UserStatus? {
        return UserStatusTable.selectAll()
            .where { UserStatusTable.userId eq userId }
            .mapNotNull(::rowToUserStatus).singleOrNull()
    }
}

