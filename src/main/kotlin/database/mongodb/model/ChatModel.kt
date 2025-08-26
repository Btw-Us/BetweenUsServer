/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:03 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mongodb.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class PersonalChatRoom(
    @param:BsonId
    val id: String = ObjectId().toString(),
    val userId: String,
    val friendId: String,
    val userName: String,
    val friendName: String,
    val friendProfileUrl: String,
    val userProfileUrl: String,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)


enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE
}

@Serializable
enum class MessageSenderType {
    USER,
    FRIEND
}

@Serializable
data class Message(
    @param:BsonId
    val id: String = ObjectId().toString(),
    val chatRoomId: String,
    val userId: String,
    val friendId: String,
    val message: String,
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null,
    val replyToMessage: String? = null,
    val senderType: MessageSenderType = MessageSenderType.USER,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false
)