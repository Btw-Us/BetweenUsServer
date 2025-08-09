/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:22 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mangodb.repository.impl

import com.aatech.database.mangodb.model.ChatEntryModel
import com.aatech.database.mangodb.repository.ChatEntryRepository
import com.aatech.plugin.configureMongoDB
import com.mongodb.kotlin.client.coroutine.MongoDatabase

class ChatEntryRepositoryImpl(
    private val database: MongoDatabase = configureMongoDB()
) : ChatEntryRepository {
    override suspend fun addChatEntry(model: ChatEntryModel): String {
        TODO("Not yet implemented")
    }

    override suspend fun getChatEntries(
        chatId: String,
        limit: Int,
        offset: Int
    ): List<ChatEntryModel> {
        TODO("Not yet implemented")
    }
}