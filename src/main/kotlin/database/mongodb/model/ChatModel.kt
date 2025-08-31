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
    val friendUserName: String,
    val userFullName: String,
    val friendFullName: String,
    val friendProfileUrl: String,
    val userProfileUrl: String,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val messageState: MessageState = MessageState.PENDING,
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
enum class MessageState {
    PENDING,
    SEND,
    DELIVER,
    READ
}

@Serializable
data class Message(
    @param:BsonId
    val id: String = ObjectId().toString(),
    val chatRoomId: String,
    val fromUid: String,
    val toUid: String,
    val message: String,
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null,
    val replyToMessage: String? = null,
    val messageState: MessageState = MessageState.PENDING
)

@Serializable
sealed class PersonalChatChangeEvent {
    abstract val timestamp: Long

    @Serializable
    data class Connected(
        override val timestamp: Long = System.currentTimeMillis()
    ) : PersonalChatChangeEvent()

    @Serializable
    data class Insert(
        val data: PersonalChatRoom,
        override val timestamp: Long
    ) : PersonalChatChangeEvent()

    @Serializable
    data class Update(
        val data: PersonalChatRoom,
        override val timestamp: Long
    ) : PersonalChatChangeEvent()

    @Serializable
    data class Delete(
        val deletedId: String,
        override val timestamp: Long
    ) : PersonalChatChangeEvent()
}