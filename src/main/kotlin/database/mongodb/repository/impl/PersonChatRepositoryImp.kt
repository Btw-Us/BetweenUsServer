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

    override suspend fun getInitialPersonalChats(userID: String): List<PersonalChatRoom> {
        return personalChatCollection
            .find(
                Filters.or(
                    Filters.eq("userId", userID),
                    Filters.eq("friendId", userID)
                )
            )
            .sort(Sorts.descending("lastMessageTime"))
            .toList()
    }

    override fun watchPersonalChats(userId: String): Flow<List<PersonalChatRoom>> {
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

                    OperationType.DELETE -> {
                        true
                    }

                    else -> false
                }
            }
            .scan(runBlocking { getInitialPersonalChats(userId) }) { currentList, changeStreamDocument ->
                try {
                    when (changeStreamDocument.operationType) {
                        OperationType.INSERT -> {
                            val document = changeStreamDocument.fullDocument
                            if (document != null && (document.userId == userId || document.friendId == userId)) {
                                println("INSERT: Adding document ${document.id} for user $userId")
                                currentList + document
                            } else {
                                currentList
                            }
                        }

                        OperationType.UPDATE, OperationType.REPLACE -> {
                            val document = changeStreamDocument.fullDocument
                            if (document != null && (document.userId == userId || document.friendId == userId)) {
                                println("UPDATE/REPLACE: Updating document ${document.id} for user $userId")
                                // Remove old version if exists, add new version
                                val filteredList = currentList.filter { it.id != document.id }
                                filteredList + document
                            } else {
                                currentList
                            }
                        }

                        OperationType.DELETE -> {
                            val documentId = changeStreamDocument.documentKey?.get("_id")?.asString()?.value
                            if (documentId != null) {
                                println("DELETE: Removing document $documentId for user $userId")
                                println(
                                    "$currentList"
                                )
                                currentList.filter { it.id != documentId }
                            } else {
                                println("DELETE: No document key found")
                                currentList
                            }
                        }

                        else -> {
                            println("Other operation: ${changeStreamDocument.operationType}")
                            currentList
                        }
                    }
                } catch (e: Exception) {
                    println("Error processing change stream document: ${e.message}")
                    e.printStackTrace()
                    currentList // Return current list on error
                }
            }
            .distinctUntilChanged() // Only emit when the list actually changes
            .onStart {
                println("Starting change stream for user $userId")
            }
            .onEach { chatRooms ->
                println("Emitting ${chatRooms.size} chat rooms for user $userId")
            }
            .catch { e ->
                println("Error in change stream for user $userId: ${e.message}")
                e.printStackTrace()
                // Get fresh data from database
                try {
                    val freshData = getInitialPersonalChats(userId)
                    emit(freshData)
                } catch (dbError: Exception) {
                    println("Failed to get fresh data: ${dbError.message}")
                    emit(emptyList())
                }
            }
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