/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 26, 2025 12:42 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.usecase

import com.aatech.database.mongodb.model.Message
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.mysql.model.UserFriendsTable
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import com.aatech.database.utils.TransactionResult
import com.aatech.database.utils.mysqlAndMongoTransactions
import com.aatech.utils.generateUuidFromSub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.update

data class CreateChatRoomUseCase(
    private val personalChatRepository: PersonChatRepository,
    private val userInteraction: UserInteractionRepository
) {
    suspend operator fun invoke(
        userId: String,
        friendsId: String,
        message: String
    ): TransactionResult<Unit, String> = withContext(Dispatchers.IO) {

        val existingChatRoomId = userInteraction.checkHasChatRoomId(
            userId = userId,
            friendId = friendsId
        )
        if (!existingChatRoomId.isNullOrBlank()) {
            personalChatRepository.addChatEntry(
                Message(
                    chatRoomId = existingChatRoomId,
                    fromUid = userId,
                    toUid = friendsId,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
            )
            return@withContext TransactionResult.Success(
                Unit,
                existingChatRoomId
            )
        }

        val chatRoomId = (userId + friendsId).generateUuidFromSub().toString()
        val userDetails = userInteraction.getUserById(
            userId
        ) ?: throw Exception("User not found")

        val friendsDetails = userInteraction.getUserById(
            friendsId
        ) ?: throw Exception("Friend not found")

        mysqlAndMongoTransactions(
            mysqlTransaction = {
                userInteraction.insertChatRoomId(
                    userId = userId,
                    friendId = friendsId,
                    chatRoomId = chatRoomId
                ).let {
                    if (!it) {
                        throw Exception("Failed to insert chat room id")
                    }
                }
            },
            mongoTransaction = {
                val personalChatRoom = PersonalChatRoom(
                    id = chatRoomId,
                    userId = userId,
                    friendId = friendsId,
                    userName = userDetails.username,
                    friendUserName = friendsDetails.username,
                    userProfileUrl = userDetails.profilePicture ?: "",
                    friendProfileUrl = friendsDetails.profilePicture ?: "",
                    userFullName = userDetails.fullName,
                    friendFullName = friendsDetails.fullName,
                    lastMessage = message,
                    lastMessageTime = System.currentTimeMillis(),
                    unreadCount = 0,
                    isPinned = false,
                    isArchived = false
                )
                val personalChatRoomId = personalChatRepository.createPersonalChatRoom(
                    personalChatRoom
                )
                val message = Message(
                    chatRoomId = personalChatRoomId,
                    fromUid = userId,
                    toUid = friendsId,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
                personalChatRepository.addChatEntry(
                    message
                )
                personalChatRoomId
            },
            rollbackMysql = {
                UserFriendsTable.update(
                    where = {
                        ((UserFriendsTable.userId eq userId) and (UserFriendsTable.friendId eq friendsId)) or
                                ((UserFriendsTable.userId eq friendsId) and (UserFriendsTable.friendId eq userId))
                    },
                ) {
                    it[UserFriendsTable.chatRoomPath] = null
                }
            },
            rollbackMongo = {
                personalChatRepository.rollBackCreatePersonalChatRoomWithMessage(it)
            },
        )
    }
}