/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:18 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mangodb.repository

import com.aatech.database.mangodb.model.Message
import com.aatech.database.mangodb.model.PersonalChatRoom

interface PersonChatRepository {
    suspend fun createChat(model : PersonalChatRoom): String

    suspend fun getAllChats(userId: String, limit: Int = 50, offset: Int = 0): List<PersonalChatRoom>

    suspend fun addChatEntry(
        model: Message
    ): String

    suspend fun getChatEntries(chatId: String, limit: Int = 50, offset: Int = 0): List<Message>


}