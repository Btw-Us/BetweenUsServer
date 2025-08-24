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
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.utils.PaginatedResponse
import com.aatech.database.utils.PaginationRequest
import kotlinx.coroutines.flow.Flow

interface PersonChatRepository {
    suspend fun createChat(model: PersonalChatRoom): String

    suspend fun getChats(
        userID: String,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom>

    suspend fun addChatEntry(model: Message): String

    fun watchPersonalChats(
        userId: String,
        paginationRequest: PaginationRequest
    ): Flow<PaginatedResponse<PersonalChatRoom>>

    fun watchChatEntries(
        personalChatRoomId: String
    ): Flow<List<Message>>
}