/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 10:17 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.user.impl

import com.aatech.data.mysql.model.Friend
import com.aatech.data.mysql.model.FriendsTable
import com.aatech.data.mysql.model.FriendshipStatus
import com.aatech.data.mysql.repository.user.FriendsRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

class FriendsRepositoryImp : FriendsRepository {
    override suspend fun addFriend(userId: String, friendId: String): Boolean {
        val query = FriendsTable.insert {
            it[id] = UUID.randomUUID().toString()
            it[requesterId] = userId
            it[receiverId] = friendId
        }
        if (query.insertedCount > 0) {
            return true
        } else {
            throw Exception("Failed to add friend")
        }
    }

    override suspend fun removeFriend(userId: String, friendId: String): Boolean {
        val query = FriendsTable.deleteWhere {
            (FriendsTable.requesterId eq userId) and (FriendsTable.receiverId eq friendId)
        }
        if (query > 0) {
            return true
        } else {
            throw Exception("Failed to remove friend")
        }
    }

    override suspend fun getFriendsList(
        userId: String,
        friendShipStatus: FriendshipStatus,
    ): List<Friend> {
        return FriendsTable.selectAll()
            .where { FriendsTable.requesterId eq userId }
            .andWhere { FriendsTable.status eq friendShipStatus.name }
            .orWhere { FriendsTable.receiverId eq userId }
            .andWhere { FriendsTable.status eq friendShipStatus.name }
            .map(::rowToFriend)
            .toList()
    }

    override suspend fun isFriend(userId: String, friendId: String): Boolean {
        return FriendsTable.selectAll()
            .where {
                (FriendsTable.requesterId eq userId and (FriendsTable.receiverId eq friendId)) or
                        (FriendsTable.requesterId eq friendId and (FriendsTable.receiverId eq userId))
            }
            .andWhere { FriendsTable.status eq FriendshipStatus.ACCEPTED.name }
            .count() > 0
    }

}

fun rowToFriend(row: ResultRow): Friend {
    return Friend(
        id = row[FriendsTable.id],
        requesterId = row[FriendsTable.requesterId],
        receiverId = row[FriendsTable.receiverId],
        status = FriendshipStatus.valueOf(row[FriendsTable.status]),
        createdAt = row[FriendsTable.createdAt],
        updatedAt = row[FriendsTable.updatedAt],
        requestedAt = row[FriendsTable.requestedAt],
        respondedAt = row[FriendsTable.respondedAt]
    )
}