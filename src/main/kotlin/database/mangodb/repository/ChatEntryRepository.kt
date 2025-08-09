/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:21 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mangodb.repository

import com.aatech.database.mangodb.model.ChatEntryModel

interface ChatEntryRepository {
    suspend fun addChatEntry(
        model: ChatEntryModel
    ): String

    suspend fun getChatEntries(chatId: String, limit: Int = 50, offset: Int = 0): List<ChatEntryModel>


}