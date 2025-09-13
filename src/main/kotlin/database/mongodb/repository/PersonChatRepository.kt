/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:18 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mongodb.repository

import com.aatech.database.mongodb.model.Message
import com.aatech.database.mongodb.model.MessageChangeEvent
import com.aatech.database.mongodb.model.PersonalChatChangeEvent
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.utils.PaginatedResponse
import com.aatech.database.utils.PaginationRequest
import kotlinx.coroutines.flow.Flow

interface PersonChatRepository {
    suspend fun createPersonalChatRoom(model: PersonalChatRoom): String

    suspend fun checkHasPersonalChatRoom(
        userID: String,
        friendID: String
    ): Boolean

    suspend fun getAllPersonalChatRoom(
        userID: String,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom>

    fun watchPersonalChats(
        userId: String,
    ): Flow<PersonalChatChangeEvent>

    suspend fun addChatEntry(model: Message): String

    suspend fun getChatEntries(
        personalChatRoomId: String,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<Message>

    fun watchChatEntries(
        personalChatRoomId: String
    ): Flow<MessageChangeEvent>


    suspend fun rollBackCreatePersonalChatRoomWithMessage(id: String)
}