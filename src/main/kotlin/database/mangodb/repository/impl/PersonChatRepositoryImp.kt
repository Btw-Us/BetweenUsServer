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
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonChatRepositoryImp @Inject constructor(
    database: MongoDatabase = configureMongoDB()
) : PersonChatRepository {

    private val personalChatCollection =
        database.getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)
    private val messageCollection = database.getCollection<Message>(MongoDbCollectionNames.Message.cName)
    override suspend fun createChat(model: PersonalChatRoom): String {
        return try {
            val result = personalChatCollection.insertOne(model)
            result.insertedId?.asString()?.value ?: throw Exception("Failed to create chat")
        } catch (e: Exception) {
            throw Exception("Error creating chat: ${e.message}", e)
        }
    }

    override suspend fun addChatEntry(model: Message): String {
        return try {
            val result = messageCollection.insertOne(model)
            result.insertedId?.asString()?.value ?: throw Exception("Failed to add chat entry")
        } catch (e: Exception) {
            throw Exception("Error adding chat entry: ${e.message}", e)
        }
    }

    override fun watchPersonalChats(userId: String): Flow<PersonalChatRoom> {
        val pipeline = mutableListOf(
            Aggregates.match(
                Filters.`in`(
                    "operationType",
                    listOf("insert", "update", "replace", "delete")
                )
            )
        )
        pipeline.add(
            Aggregates.match(
                Filters.or(
                    Filters.eq("userId", userId),
                    Filters.eq("friendId", userId)
                )
            )
        )
        return personalChatCollection.watch(pipeline)
            .fullDocument(com.mongodb.client.model.changestream.FullDocument.UPDATE_LOOKUP)
            .map { changeStreamDocument ->
                changeStreamDocument.fullDocument ?: throw Exception("No full document found")
            }
    }

    override fun watchChatEntries(personalChatRoomId: String): Flow<Message> {
        val pipeline = mutableListOf(
            Aggregates.match(
                Filters.`in`(
                    "operationType",
                    listOf("insert", "update", "replace", "delete")
                )
            )
        )
        pipeline.add(
            Aggregates.match(
                Filters.or(
                    Filters.eq("chatRoomId", personalChatRoomId),
                )
            )
        )
        return messageCollection.watch(pipeline)
            .fullDocument(com.mongodb.client.model.changestream.FullDocument.UPDATE_LOOKUP)
            .map { changeStreamDocument ->
                changeStreamDocument.fullDocument ?: throw Exception("No full document found")
            }
    }

}