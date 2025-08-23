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
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.utils.PaginatedResponse
import com.aatech.database.utils.PaginationInfo
import com.aatech.database.utils.PaginationRequest
import com.aatech.plugin.configureMongoDB
import com.aatech.utils.MongoDbCollectionNames
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class PersonChatRepositoryImp(
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

    suspend fun getPersonalChatsWithPagination(
        userId: String,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom> {

        val filter = Filters.or(
            Filters.eq("userId", userId),
            Filters.eq("friendId", userId)
        )

        // Get total count
        val totalItems = personalChatCollection.countDocuments(filter)


        // Get paginated data
        val chatRooms = personalChatCollection
            .find(filter)
            .skip(paginationRequest.offset)
            .limit(paginationRequest.limit)
            .toList()

        val totalPages = ((totalItems + paginationRequest.size - 1) / paginationRequest.size).toInt()

        val paginationInfo = PaginationInfo(
            currentPage = paginationRequest.page,
            pageSize = paginationRequest.size,
            totalItems = totalItems,
            totalPages = totalPages,
            hasNext = paginationRequest.page < totalPages,
            hasPrevious = paginationRequest.page > 1
        )

        return PaginatedResponse(chatRooms, paginationInfo)
    }

    override suspend fun getInitialPersonalChats(
        userID: String,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom> {
        return getPersonalChatsWithPagination(userID, paginationRequest)
    }

    suspend fun getInitialPersonalChatsWithPagination(
        userId: String,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<PersonalChatRoom> {
        return getPersonalChatsWithPagination(userId, paginationRequest)
    }

    override fun watchPersonalChats(
        userId: String,
        paginationRequest: PaginationRequest
    ): Flow<PaginatedResponse<PersonalChatRoom>> {
        val pipeline = listOf(
            Aggregates.match(
                Filters.or(
                    Filters.and(
                        Filters.`in`("operationType", listOf("insert", "update", "replace")),
                        Filters.or(
                            Filters.eq("fullDocument.userId", userId),
                            Filters.eq("fullDocument.friendId", userId)
                        )
                    ),
                    Filters.eq("operationType", "delete")
                )
            )
        )

        return personalChatCollection.watch(pipeline)
            .fullDocument(FullDocument.UPDATE_LOOKUP)
            .filter { changeStreamDocument ->
                when (changeStreamDocument.operationType) {
                    OperationType.INSERT, OperationType.UPDATE, OperationType.REPLACE -> {
                        val document = changeStreamDocument.fullDocument
                        document != null && (document.userId == userId || document.friendId == userId)
                    }

                    OperationType.DELETE -> true
                    else -> false
                }
            }
            .scan(runBlocking {
                getInitialPersonalChatsWithPagination(
                    userId,
                    paginationRequest
                )
            }) { currentResponse, changeStreamDocument ->
                try {
                    val currentList = currentResponse.data.toMutableList()

                    when (changeStreamDocument.operationType) {
                        OperationType.INSERT -> {
                            val document = changeStreamDocument.fullDocument
                            if (document != null && (document.userId == userId || document.friendId == userId)) {
                                println("INSERT: Adding document ${document.id} for user $userId")
                                currentList.add(0, document) // Add to beginning for real-time updates
                            }
                        }

                        OperationType.UPDATE, OperationType.REPLACE -> {
                            val document = changeStreamDocument.fullDocument
                            if (document != null && (document.userId == userId || document.friendId == userId)) {
                                println("UPDATE/REPLACE: Updating document ${document.id} for user $userId")
                                val index = currentList.indexOfFirst { it.id == document.id }
                                if (index != -1) {
                                    currentList[index] = document
                                }
                            }
                        }

                        OperationType.DELETE -> {
                            val documentId = changeStreamDocument.documentKey?.get("_id")?.asString()?.value
                            if (documentId != null) {
                                println("DELETE: Removing document $documentId for user $userId")
                                currentList.removeIf { it.id == documentId }
                            }
                        }

                        else -> {
                            println("Other operation: ${changeStreamDocument.operationType}")
                        }
                    }

                    // Recalculate pagination info
                    val newPaginationInfo = currentResponse.pagination.copy(
                        totalItems = currentList.size.toLong(),
                        totalPages = ((currentList.size + paginationRequest.size - 1) / paginationRequest.size)
                    )

                    PaginatedResponse(currentList, newPaginationInfo)
                } catch (e: Exception) {
                    println("Error processing change stream document: ${e.message}")
                    e.printStackTrace()
                    currentResponse
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun watchChatEntries(personalChatRoomId: String): Flow<List<Message>> {
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
            .fullDocument(FullDocument.UPDATE_LOOKUP)
            .scan(emptyList()) { currentList, changeStreamDocument ->
                val document = changeStreamDocument.fullDocument
                    ?: throw Exception("No full document found")
                when (changeStreamDocument.operationType) {
                    OperationType.INSERT -> currentList + document
                    OperationType.UPDATE, OperationType.REPLACE -> {
                        currentList.map { if (it.id == document.id) document else it }
                    }

                    OperationType.DELETE -> {
                        currentList.filter { it.id != document.id }
                    }

                    else -> currentList
                }
            }
    }

}