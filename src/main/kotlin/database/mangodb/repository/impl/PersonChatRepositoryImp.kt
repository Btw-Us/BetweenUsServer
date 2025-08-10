/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:36 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mangodb.repository.impl

import com.aatech.database.mangodb.model.Message
import com.aatech.database.mangodb.model.PersonalChatRoom
import com.aatech.database.mangodb.repository.PersonChatRepository
import com.aatech.plugin.configureMongoDB
import com.aatech.utils.MongoDbCollectionNames
import com.mongodb.kotlin.client.coroutine.MongoDatabase

class PersonChatRepositoryImp(
    database: MongoDatabase = configureMongoDB()
) : PersonChatRepository {

    private val collection = database.getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)
    override suspend fun createChat(model: PersonalChatRoom): String {
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
    ): List<PersonalChatRoom> {
        return emptyList()
    }

    override suspend fun addChatEntry(model: Message): String {
        TODO("Not yet implemented")
    }

    override suspend fun getChatEntries(
        chatId: String,
        limit: Int,
        offset: Int
    ): List<Message> {
        TODO("Not yet implemented")
    }

}