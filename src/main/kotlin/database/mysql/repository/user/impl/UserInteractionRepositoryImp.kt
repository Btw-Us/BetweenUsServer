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

import com.aatech.database.mysql.mapper.rowToFriendRequest
import com.aatech.database.mysql.mapper.rowToUser
import com.aatech.database.mysql.model.FriendsRequestTable
import com.aatech.database.mysql.model.FriendshipRequestStatus
import com.aatech.database.mysql.model.UserFriendsTable
import com.aatech.database.mysql.model.UserTable
import com.aatech.database.mysql.model.entity.SearchUserResponse
import com.aatech.database.mysql.model.entity.User
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class UserInteractionRepositoryImp : UserInteractionRepository {
    override suspend fun findFriends(
        loggedUserId: String, userName: String
    ): List<SearchUserResponse> = withContext(Dispatchers.IO) {
        transaction {
            val friendsAsRequester = FriendsRequestTable.alias("friends_as_requester")
            val friendsAsReceiver = FriendsRequestTable.alias("friends_as_receiver")

            joinUserAndFriendsTable(friendsAsRequester, loggedUserId, friendsAsReceiver).selectAll().where {
                (UserTable.username like "%$userName%") and (UserTable.uuid neq loggedUserId) and
                        ((friendsAsRequester[FriendsRequestTable.status].isNull()
                                or (friendsAsRequester[FriendsRequestTable.status] neq FriendshipRequestStatus.ACCEPTED.name))
                                and (friendsAsReceiver[FriendsRequestTable.status].isNull() or (friendsAsReceiver[FriendsRequestTable.status]
                                neq FriendshipRequestStatus.ACCEPTED.name))) and ((friendsAsRequester[FriendsRequestTable.status].isNull()
                        or (friendsAsRequester[FriendsRequestTable.status] neq FriendshipRequestStatus.BLOCKED.name))
                        and (friendsAsReceiver[FriendsRequestTable.status].isNull() or (friendsAsReceiver[FriendsRequestTable.status]
                        neq FriendshipRequestStatus.BLOCKED.name)))
            }.map {
                rowToUserAndFriend(
                    it,
                    friendsAsRequester,
                    friendsAsReceiver,
                    loggedUserId
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
                    val existingFriendship = FriendsRequestTable.selectAll().where {
                        (FriendsRequestTable.requesterId eq userId and (FriendsRequestTable.receiverId eq friendId)) or (FriendsRequestTable.requesterId eq friendId and (FriendsRequestTable.receiverId eq userId))
                    }.firstOrNull()
                    if (existingFriendship != null) {
                        val deletedRow = FriendsRequestTable.deleteWhere {
                            (FriendsRequestTable.requesterId eq userId and (FriendsRequestTable.receiverId eq friendId)) or (FriendsRequestTable.requesterId eq friendId and (FriendsRequestTable.receiverId eq userId))
                        }
                        if (deletedRow > 0) {
                            UserInteractionRepository.FriendshipAction.UNSEND
                        } else {
                            throw Exception("Failed to delete existing friendship request")
                        }
                    } else {
                        val insertedRow = FriendsRequestTable.insert {
                            it[id] = userId + friendId
                            it[requesterId] = userId
                            it[receiverId] = friendId
                            it[status] = FriendshipRequestStatus.PENDING.name
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

    override suspend fun getAllFriends(userId: String): List<User> {
        return withContext(Dispatchers.IO) {
            transaction {
                val friendsAsRequester = FriendsRequestTable.alias("friends_as_requester")
                val friendsAsReceiver = FriendsRequestTable.alias("friends_as_receiver")

                joinUserAndFriendsTable(friendsAsRequester, userId, friendsAsReceiver)
                    .selectAll()
                    .where {
                        (friendsAsRequester[FriendsRequestTable.status] eq FriendshipRequestStatus.ACCEPTED.name) or
                                (friendsAsReceiver[FriendsRequestTable.status] eq FriendshipRequestStatus.ACCEPTED.name)
                    }.map(::rowToUser)
            }
        }
    }

    private fun joinUserAndFriendsTable(
        friendsAsRequester: Alias<FriendsRequestTable>,
        userId: String,
        friendsAsReceiver: Alias<FriendsRequestTable>
    ): Join = UserTable.leftJoin(
        friendsAsRequester,
        { UserTable.uuid },
        { friendsAsRequester[FriendsRequestTable.requesterId] },
        additionalConstraint = {
            friendsAsRequester[FriendsRequestTable.receiverId] eq userId
        }).leftJoin(
        friendsAsReceiver,
        { UserTable.uuid },
        { friendsAsReceiver[FriendsRequestTable.receiverId] },
        additionalConstraint = {
            friendsAsReceiver[FriendsRequestTable.requesterId] eq userId
        })

    override suspend fun getAllReceivedRequests(userId: String): List<SearchUserResponse> =
        withContext(Dispatchers.IO) {
            transaction {
                val friendsAsReceiver = FriendsRequestTable.alias("friends_as_receiver")
                UserTable.leftJoin(
                    friendsAsReceiver,
                    { UserTable.uuid },
                    { friendsAsReceiver[FriendsRequestTable.requesterId] },
                    additionalConstraint = {
                        friendsAsReceiver[FriendsRequestTable.receiverId] eq userId
                    }
                ).selectAll().where {
                    (friendsAsReceiver[FriendsRequestTable.status] eq FriendshipRequestStatus.PENDING.name) and (UserTable.uuid neq userId)
                }.map {
                    rowToUserAndFriend(
                        it,
                        FriendsRequestTable.alias("friends_as_requester"),
                        friendsAsReceiver,
                        userId
                    )
                }
            }
        }

    override suspend fun getAllSentRequests(userId: String): List<SearchUserResponse> =
        withContext(Dispatchers.IO) {
            transaction {
                val friendsAsRequester = FriendsRequestTable.alias("friends_as_requester")
                UserTable.leftJoin(
                    friendsAsRequester,
                    { UserTable.uuid },
                    { friendsAsRequester[FriendsRequestTable.receiverId] },
                    additionalConstraint = {
                        friendsAsRequester[FriendsRequestTable.requesterId] eq userId
                    }
                ).selectAll().where {
                    (friendsAsRequester[FriendsRequestTable.status] eq FriendshipRequestStatus.PENDING.name) and (UserTable.uuid neq userId)
                }.map {
                    rowToUserAndFriend(
                        it,
                        friendsAsRequester,
                        FriendsRequestTable.alias("friends_as_receiver"),
                        userId
                    )
                }
            }
        }


    override suspend fun responseToFriendRequest(
        userId: String,
        friendId: String,
        requestStatus: FriendshipRequestStatus
    ): FriendshipRequestStatus = withContext(Dispatchers.IO) {
        transaction {
            val reqEntity = FriendsRequestTable.selectAll()
                .where {
                    FriendsRequestTable.id eq friendId + userId
                }.map(::rowToFriendRequest)
                .firstOrNull()
            if (reqEntity == null) {
                throw Exception("Friend request not found")
            }
            if (reqEntity.status == FriendshipRequestStatus.PENDING) {
                throw Exception("Friend request is still pending")
            }
            if (reqEntity.receiverId != userId) {
                throw Exception("You are not the receiver of this friend request")
            }

            if (requestStatus == FriendshipRequestStatus.DECLINED) {
                FriendsRequestTable.deleteWhere {
                    FriendsRequestTable.id eq reqEntity.id
                }
                return@transaction FriendshipRequestStatus.DECLINED
            }
            if (requestStatus == FriendshipRequestStatus.BLOCKED) {
//                From here, we will delete the friend request and also remove the friendship if it exists
                FriendsRequestTable.deleteWhere {
                    FriendsRequestTable.id eq reqEntity.id or
                            (FriendsRequestTable.requesterId eq reqEntity.receiverId and (FriendsRequestTable.receiverId eq reqEntity.requesterId)) or
                            (FriendsRequestTable.requesterId eq reqEntity.requesterId and (FriendsRequestTable.receiverId eq reqEntity.receiverId))
                }

//               We will also delete the friendship if it exists
                UserFriendsTable.deleteWhere {
                    (UserFriendsTable.userId eq reqEntity.requesterId) and (UserFriendsTable.friendId eq reqEntity.receiverId) or
                            (UserFriendsTable.userId eq reqEntity.receiverId) and (UserFriendsTable.friendId eq reqEntity.requesterId)
                }
                return@transaction FriendshipRequestStatus.BLOCKED
            }
            FriendsRequestTable.update({ FriendsRequestTable.id eq reqEntity.id }) {
                it[status] = requestStatus.name
                it[respondedAt] = System.currentTimeMillis()
            }
            UserFriendsTable.insert {
                it[UserFriendsTable.userId] = reqEntity.requesterId
                it[UserFriendsTable.friendId] = reqEntity.receiverId
                it[UserFriendsTable.friendshipRequestStatus] = requestStatus.name
            }

        }
        FriendshipRequestStatus.ACCEPTED
    }


    fun rowToUserAndFriend(
        row: ResultRow,
        friendsAsRequester: Alias<FriendsRequestTable>,
        friendsAsReceiver: Alias<FriendsRequestTable>,
        loggedUserId: String
    ): SearchUserResponse {
        val user = rowToUser(row)
        val requesterStatus = row.getOrNull(friendsAsRequester[FriendsRequestTable.status])?.let {
            FriendshipRequestStatus.valueOf(it)
        }
        val receiverStatus = row.getOrNull(friendsAsReceiver[FriendsRequestTable.status])?.let {
            FriendshipRequestStatus.valueOf(it)
        }
        val friendshipStatus = requesterStatus ?: receiverStatus
        val isLoggedUserCreatedReq = when (friendshipStatus) {
            null -> null
            else -> {
                val receiverIdFromReceiver = row.getOrNull(friendsAsReceiver[FriendsRequestTable.receiverId])
                val receiverIdFromRequester = row.getOrNull(friendsAsRequester[FriendsRequestTable.receiverId])
                (receiverIdFromReceiver ?: receiverIdFromRequester) == loggedUserId
            }
        }?.not()

        return SearchUserResponse(user, friendshipStatus, isLoggedUserCreatedReq)
    }
}