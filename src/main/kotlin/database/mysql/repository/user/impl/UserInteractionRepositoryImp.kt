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
import com.aatech.database.mysql.model.entity.User
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserInteractionRepositoryImp : UserInteractionRepository {
    override suspend fun findFriends(
        loggedUserId: String,
        userName: String
    ): List<User> = withContext(Dispatchers.IO) {
        transaction {
            val friendsAsRequester = FriendsTable.alias("friends_as_requester")
            val friendsAsReceiver = FriendsTable.alias("friends_as_receiver")

            UserTable
                .leftJoin(
                    friendsAsRequester,
                    { UserTable.uuid },
                    { friendsAsRequester[FriendsTable.requesterId] },
                    additionalConstraint = {
                        friendsAsRequester[FriendsTable.receiverId] eq loggedUserId
                    }
                )
                .leftJoin(
                    friendsAsReceiver,
                    { UserTable.uuid },
                    { friendsAsReceiver[FriendsTable.receiverId] },
                    additionalConstraint = {
                        friendsAsReceiver[FriendsTable.requesterId] eq loggedUserId
                    }
                )
                .selectAll()
                .where {
                    (UserTable.username like "%$userName%") and
                            (UserTable.uuid neq loggedUserId) and
                            // User is not already friends (neither as requester nor receiver)
                            (
                                    (friendsAsRequester[FriendsTable.status].isNull() or
                                            (friendsAsRequester[FriendsTable.status] neq FriendshipStatus.ACCEPTED.name)) and
                                            (friendsAsReceiver[FriendsTable.status].isNull() or
                                                    (friendsAsReceiver[FriendsTable.status] neq FriendshipStatus.ACCEPTED.name))
                                    ) and
                            // User is not blocked (neither as requester nor receiver)
                            (
                                    (friendsAsRequester[FriendsTable.status].isNull() or
                                            (friendsAsRequester[FriendsTable.status] neq FriendshipStatus.BLOCKED.name)) and
                                            (friendsAsReceiver[FriendsTable.status].isNull() or
                                                    (friendsAsReceiver[FriendsTable.status] neq FriendshipStatus.BLOCKED.name))
                                    )
                }
                .map(::rowToUser)
        }
    }

    override fun sendFriendRequest(userId: String, friendId: String): Boolean {
        TODO("Not yet implemented")
    }
}