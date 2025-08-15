/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 13, 2025 09:02 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.mysql.repository.user.impl

import com.aatech.database.mysql.mapper.rowToUser
import com.aatech.database.mysql.model.FriendsTable
import com.aatech.database.mysql.model.FriendshipStatus
import com.aatech.database.mysql.model.UserTable
import com.aatech.database.mysql.model.entity.SearchUserResponse
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserInteractionRepositoryImp : UserInteractionRepository {
    override suspend fun findFriends(
        loggedUserId: String, userName: String
    ): List<SearchUserResponse> = withContext(Dispatchers.IO) {
        transaction {
            val friendsAsRequester = FriendsTable.alias("friends_as_requester")
            val friendsAsReceiver = FriendsTable.alias("friends_as_receiver")

            UserTable.leftJoin(
                friendsAsRequester,
                { UserTable.uuid },
                { friendsAsRequester[FriendsTable.requesterId] },
                additionalConstraint = {
                    friendsAsRequester[FriendsTable.receiverId] eq loggedUserId
                }).leftJoin(
                friendsAsReceiver,
                { UserTable.uuid },
                { friendsAsReceiver[FriendsTable.receiverId] },
                additionalConstraint = {
                    friendsAsReceiver[FriendsTable.requesterId] eq loggedUserId
                }).selectAll().where {
                (UserTable.username like "%$userName%") and (UserTable.uuid neq loggedUserId) and ((friendsAsRequester[FriendsTable.status].isNull() or (friendsAsRequester[FriendsTable.status] neq FriendshipStatus.ACCEPTED.name)) and (friendsAsReceiver[FriendsTable.status].isNull() or (friendsAsReceiver[FriendsTable.status] neq FriendshipStatus.ACCEPTED.name))) and ((friendsAsRequester[FriendsTable.status].isNull() or (friendsAsRequester[FriendsTable.status] neq FriendshipStatus.BLOCKED.name)) and (friendsAsReceiver[FriendsTable.status].isNull() or (friendsAsReceiver[FriendsTable.status] neq FriendshipStatus.BLOCKED.name)))
            }.map {
                rowToUserAndFriend(
                    it, friendsAsRequester, friendsAsReceiver
                )
            }
        }
    }

    override suspend fun sendOrUnsendFriendRequest(
        userId: String,
        friendId: String
    ): UserInteractionRepository.FriendshipAction =
        withContext(Dispatchers.IO) {
            transaction {
                try {
                    val existingFriendship = FriendsTable.selectAll().where {
                        (FriendsTable.requesterId eq userId and (FriendsTable.receiverId eq friendId)) or (FriendsTable.requesterId eq friendId and (FriendsTable.receiverId eq userId))
                    }.firstOrNull()
                    if (existingFriendship != null) {
                        val deletedRow = FriendsTable.deleteWhere {
                            (FriendsTable.requesterId eq userId and (FriendsTable.receiverId eq friendId)) or (FriendsTable.requesterId eq friendId and (FriendsTable.receiverId eq userId))
                        }
                        if (deletedRow > 0) {
                            UserInteractionRepository.FriendshipAction.UNSEND
                        } else {
                            throw Exception("Failed to delete existing friendship request")
                        }
                    } else {
                        val insertedRow = FriendsTable.insert {
                            it[id] = userId + friendId
                            it[requesterId] = userId
                            it[receiverId] = friendId
                            it[status] = FriendshipStatus.PENDING.name
                            it[requestedAt] = System.currentTimeMillis()
                        }
                        if (insertedRow.insertedCount > 0) {
                            UserInteractionRepository.FriendshipAction.SEND
                        } else {
                            throw Exception("Failed to insert new friendship request")
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
        }


    fun rowToUserAndFriend(
        row: ResultRow, friendsAsRequester: Alias<FriendsTable>, friendsAsReceiver: Alias<FriendsTable>
    ): SearchUserResponse {
        val user = rowToUser(row)
        val requesterStatus = row.getOrNull(friendsAsRequester[FriendsTable.status])?.let {
            FriendshipStatus.valueOf(it)
        }
        val receiverStatus = row.getOrNull(friendsAsReceiver[FriendsTable.status])?.let {
            FriendshipStatus.valueOf(it)
        }
        val friendshipStatus = requesterStatus ?: receiverStatus
        return SearchUserResponse(user, friendshipStatus)
    }
}