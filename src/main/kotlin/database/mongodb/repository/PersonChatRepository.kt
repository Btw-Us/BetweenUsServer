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
import kotlinx.coroutines.flow.Flow

interface PersonChatRepository {
    suspend fun createChat(model: PersonalChatRoom): String

    suspend fun getInitialPersonalChats(userID: String): List<PersonalChatRoom>

    suspend fun addChatEntry(
        model: Message
    ): String

    fun watchPersonalChats(userId: String): Flow<List<PersonalChatRoom>>
    fun watchChatEntries(personalChatRoomId: String): Flow<List<Message>>
}