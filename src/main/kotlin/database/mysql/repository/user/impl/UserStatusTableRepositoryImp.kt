/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:28 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.repository.user.impl

import com.aatech.data.mysql.model.UserStatus
import com.aatech.data.mysql.model.UserStatusTable
import com.aatech.data.mysql.repository.user.UserStatusTableRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class UserStatusTableRepositoryImp : UserStatusTableRepository {
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
            .mapNotNull { row ->
                rowToUserStatus(row)
            }.singleOrNull()
    }
}

fun rowToUserStatus(row: ResultRow): UserStatus {
    return UserStatus(
        userId = row[UserStatusTable.userId],
        status = row[UserStatusTable.status],
        lastUpdated = row[UserStatusTable.lastUpdated]
    )
}