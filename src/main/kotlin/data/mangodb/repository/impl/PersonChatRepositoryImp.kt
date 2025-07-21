/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:36 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mangodb.repository.impl

import com.aatech.data.mangodb.model.PersonalChatModel
import com.aatech.data.mangodb.repository.PersonChatRepository
import com.aatech.plugin.configureMongoDB
import com.mongodb.kotlin.client.coroutine.MongoDatabase

class PersonChatRepositoryImp(
    private val database: MongoDatabase = configureMongoDB()
) : PersonChatRepository {
    private val collection = database.getCollection<PersonalChatModel>("personal_chats")

    override suspend fun createChat(model: PersonalChatModel): String {
        return try {
            val result = collection.insertOne(model)
            result.insertedId?.asString()?.value ?: throw Exception("Failed to create chat")
        } catch (e: Exception) {
            throw Exception("Error creating chat: ${e.message}", e)
        }
    }

    override suspend fun getAllChats(
        userId: String,
        limit: Int,
        offset: Int
    ): List<PersonalChatModel> {
        return emptyList()
    }

}