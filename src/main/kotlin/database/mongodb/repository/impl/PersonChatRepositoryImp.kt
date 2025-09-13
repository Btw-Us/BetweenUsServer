/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:36 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mongodb.repository.impl

import com.aatech.database.mongodb.model.Message
import com.aatech.database.mongodb.model.MessageChangeEvent
import com.aatech.database.mongodb.model.PersonalChatChangeEvent
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.utils.PaginatedResponse
import com.aatech.database.utils.PaginationInfo
import com.aatech.database.utils.PaginationRequest
import com.aatech.plugin.configureMongoDB
import com.aatech.utils.MongoDbCollectionNames
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class PersonChatRepositoryImp(
    database: MongoDatabase = configureMongoDB()
) : PersonChatRepository {

    private val personalChatCollection =
        database.getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)
    private val messageCollection = database.getCollection<Message>(MongoDbCollectionNames.Message.cName)

    override suspend fun createPersonalChatRoom(model: PersonalChatRoom): String {
        return try {
            val result = personalChatCollection.insertOne(model)
            result.insertedId?.asString()?.value ?: throw Exception("Failed to create chat")
        } catch (e: Exception) {
            throw Exception("Error creating chat: ${e.message}", e)
        }
    }


    suspend fun getPersonalChatsWithPagination(
        userId: String, paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom> {

        val filter = Filters.or(
            Filters.eq("userId", userId), Filters.eq("friendId", userId)
        )

        // Get total count
        val totalItems = personalChatCollection.countDocuments(filter)

        // Get paginated data with proper sorting
        val chatRooms = personalChatCollection.find(filter).sort(
                Sorts.orderBy(
                    Sorts.descending("lastMessageTime")
                )
            ).skip(paginationRequest.offset).limit(paginationRequest.limit) // Use limit, not pageSize
            .toList()

        // Fix: Use consistent pageSize from paginationRequest.limit
        val totalPages = if (totalItems == 0L) 1
        else ((totalItems + paginationRequest.limit - 1) / paginationRequest.limit).toInt()

        val paginationInfo = PaginationInfo(
            currentPage = paginationRequest.page,
            pageSize = paginationRequest.limit, // Use limit for consistency
            totalItems = totalItems,
            totalPages = totalPages,
            hasNext = paginationRequest.page < totalPages,
            hasPrevious = paginationRequest.page > 1
        )

        return PaginatedResponse(chatRooms, paginationInfo)
    }

    override suspend fun getAllPersonalChatRoom(
        userID: String, paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom> {
        return getPersonalChatsWithPagination(userID, paginationRequest)
    }

    override fun watchPersonalChats(
        userId: String,
    ): Flow<PersonalChatChangeEvent> {
        val pipeline = listOf(
            Aggregates.match(
                Filters.or(
                    Filters.and(
                        Filters.`in`("operationType", listOf("insert", "update", "replace")), Filters.or(
                            Filters.eq("fullDocument.userId", userId), Filters.eq("fullDocument.friendId", userId)
                        )
                    ), Filters.eq("operationType", "delete")
                )
            )
        )

        return personalChatCollection.watch(pipeline).fullDocument(FullDocument.UPDATE_LOOKUP)
            .filter { changeStreamDocument ->
                when (changeStreamDocument.operationType) {
                    OperationType.INSERT, OperationType.UPDATE, OperationType.REPLACE -> {
                        val document = changeStreamDocument.fullDocument
                        document != null && (document.userId == userId || document.friendId == userId)
                    }

                    OperationType.DELETE -> true
                    else -> false
                }
            }.map { changeStreamDocument ->
                when (changeStreamDocument.operationType) {
                    OperationType.INSERT -> {
                        PersonalChatChangeEvent.Insert(
                            data = changeStreamDocument.fullDocument!!, timestamp = System.currentTimeMillis()
                        )
                    }

                    OperationType.UPDATE, OperationType.REPLACE -> {
                        PersonalChatChangeEvent.Update(
                            data = changeStreamDocument.fullDocument!!, timestamp = System.currentTimeMillis()
                        )
                    }

                    OperationType.DELETE -> {
                        val documentId = changeStreamDocument.documentKey?.get("_id")?.asString()?.value
                        PersonalChatChangeEvent.Delete(
                            deletedId = documentId!!, timestamp = System.currentTimeMillis()
                        )
                    }

                    else -> throw IllegalStateException("Unsupported operation type")
                }
            }.flowOn(Dispatchers.IO)
    }


    suspend fun getChatsWithPagination(
        personalChatRoomId: String, paginationRequest: PaginationRequest
    ): PaginatedResponse<Message> {
        val filter = Filters.eq("chatRoomId", personalChatRoomId)

        // Get total count
        val totalItems = messageCollection.countDocuments(filter)

        // Get paginated data with proper sorting
        val messages = messageCollection.find(filter).sort(Sorts.descending("timestamp")).skip(paginationRequest.offset)
            .limit(paginationRequest.limit) // Use limit, not pageSize
            .toList()

        val totalPages = if (totalItems == 0L) 1
        else ((totalItems + paginationRequest.limit - 1) / paginationRequest.limit).toInt()

        val paginationInfo = PaginationInfo(
            currentPage = paginationRequest.page,
            pageSize = paginationRequest.limit, // Use limit for consistency
            totalItems = totalItems,
            totalPages = totalPages,
            hasNext = paginationRequest.page < totalPages,
            hasPrevious = paginationRequest.page > 1
        )

        return PaginatedResponse(messages, paginationInfo)
    }


    override suspend fun getChatEntries(
        personalChatRoomId: String, paginationRequest: PaginationRequest
    ): PaginatedResponse<Message> = getChatsWithPagination(personalChatRoomId, paginationRequest)


    override suspend fun addChatEntry(model: Message): String {
        return try {
            val result = messageCollection.insertOne(model)
            result.insertedId?.asString()?.value ?: throw Exception("Failed to add chat entry")
        } catch (e: Exception) {
            throw Exception("Error adding chat entry: ${e.message}", e)
        }
    }

    override fun watchChatEntries(
        personalChatRoomId: String
    ): Flow<MessageChangeEvent> {
        val pipeline = listOf(
            Aggregates.match(
                Filters.or(
                    Filters.and(
                        Filters.`in`("operationType", listOf("insert", "update", "replace")),
                        Filters.eq("fullDocument.chatRoomId", personalChatRoomId)
                    ), Filters.eq("operationType", "delete")
                )
            )
        )

        return messageCollection.watch(pipeline).fullDocument(FullDocument.UPDATE_LOOKUP)
            .filter { changeStreamDocument ->
                when (changeStreamDocument.operationType) {
                    OperationType.INSERT, OperationType.UPDATE, OperationType.REPLACE -> {
                        val document = changeStreamDocument.fullDocument
                        document != null && document.chatRoomId == personalChatRoomId
                    }

                    OperationType.DELETE -> true
                    else -> false
                }
            }.map { changeStreamDocument ->
                when (changeStreamDocument.operationType) {
                    OperationType.INSERT -> {
                        MessageChangeEvent.Insert(
                            data = changeStreamDocument.fullDocument!!, timestamp = System.currentTimeMillis()
                        )
                    }

                    OperationType.UPDATE, OperationType.REPLACE -> {
                        MessageChangeEvent.Update(
                            data = changeStreamDocument.fullDocument!!, timestamp = System.currentTimeMillis()
                        )
                    }

                    OperationType.DELETE -> {
                        val documentId = changeStreamDocument.documentKey?.get("_id")?.asString()?.value
                        MessageChangeEvent.Delete(
                            deletedId = documentId!!, timestamp = System.currentTimeMillis()
                        )
                    }

                    else -> throw IllegalStateException("Unsupported operation type")
                }
            }.flowOn(Dispatchers.IO)
    }

    override suspend fun rollBackCreatePersonalChatRoomWithMessage(id: String) {
        try {
            personalChatCollection.deleteOne(Filters.eq("_id", id))
            messageCollection.deleteMany(Filters.eq("chatRoomId", id))
        } catch (e: Exception) {
            throw Exception("Error during rollback: ${e.message}", e)
        }
    }

}

