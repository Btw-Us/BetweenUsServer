/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 10, 2025 04:22 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.websocket.cm

import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.utils.PaginationRequest
import com.aatech.websocket.model.ChatRoomsUpdateData
import com.aatech.websocket.model.PersonalChatConnection
import com.aatech.websocket.model.WebSocketEventSendingDataType
import com.aatech.websocket.model.WebSocketMessage
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonChatRoomConnectionManager @Inject constructor(
    private val repository: PersonChatRepository
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val connections = ConcurrentHashMap<String, MutableSet<PersonalChatConnection>>()
    private val userWatchJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun addConnection(userId: String, paginationRequest: PaginationRequest, session: WebSocketSession) {
        val connection = PersonalChatConnection(session, userId)

        connections.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(connection)
        println("User $userId connected. Total connections: ${getTotalConnections()}")

        // Send initial data
        sendInitialChatRooms(
            userId = userId, paginationRequest = paginationRequest, session = session
        )

        // Start watching for changes if not already watching
        if (!userWatchJobs.containsKey(userId)) {
            startWatchingUser(userId, paginationRequest)
        }
    }

    suspend fun removeConnection(userId: String, session: WebSocketSession) {
        connections[userId]?.removeIf { it.session == session }

        // Clean up empty sets
        if (connections[userId]?.isEmpty() == true) {
            connections.remove(userId)
            // Cancel watching if no more connections for this user
            userWatchJobs[userId]?.cancel()
            userWatchJobs.remove(userId)
            println("Stopped watching for user $userId - no more connections")
        }

        println("User $userId disconnected. Total connections: ${getTotalConnections()}")
    }

    private suspend fun sendInitialChatRooms(
        userId: String,
        paginationRequest: PaginationRequest,
        session: WebSocketSession
    ) {
        try {
            val chatRooms = repository.getInitialPersonalChats(userId, paginationRequest)
            val message = WebSocketMessage(
                type = WebSocketEventSendingDataType.INITIAL_DATA,
                data = ChatRoomsUpdateData(chatRooms)
            )
            session.send(Frame.Text(json.encodeToString(message)))
        } catch (e: Exception) {
            println("Error sending initial chat rooms to user $userId: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun startWatchingUser(
        userId: String,
        paginationRequest: PaginationRequest
    ) {
        println("Starting to watch changes for user $userId")

        val job = scope.launch {
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries && connections.containsKey(userId)) {
                try {
                    repository.watchPersonalChats(userId, paginationRequest)
                        .flowOn(Dispatchers.IO)
                        .collect { chatRooms ->
                            broadcastToUser(
                                userId,
                                WebSocketEventSendingDataType.UPDATE_DATA,
                                ChatRoomsUpdateData(chatRooms)
                            )
                        }
                    break // If we reach here, the flow completed normally
                } catch (e: CancellationException) {
                    println("Watch cancelled for user $userId")
                    break
                } catch (e: Exception) {
                    retryCount++
                    println("Error watching chat rooms for user $userId (attempt $retryCount): ${e.message}")
                    e.printStackTrace()

                    if (retryCount < maxRetries && connections.containsKey(userId)) {
                        val delayTime = (retryCount * 2000L) // Exponential backoff
                        println("Retrying watch for user $userId in ${delayTime}ms")
                        delay(delayTime)
                    }
                }
            }

            if (retryCount >= maxRetries) {
                println("Max retries reached for user $userId, stopping watch")
            }
        }

        userWatchJobs[userId] = job
    }

    private suspend fun broadcastToUser(
        userId: String,
        messageType: WebSocketEventSendingDataType,
        data: ChatRoomsUpdateData
    ) {
        val message = WebSocketMessage(type = messageType, data = data)
        val messageJson = json.encodeToString(message)

        val userConnections = connections[userId]?.toList() ?: return
        val failedConnections = mutableListOf<PersonalChatConnection>()

        userConnections.forEach { connection ->
            try {
                if (connection.session.isActive) {
                    connection.session.send(Frame.Text(messageJson))
                    println("Message sent successfully to user $userId")
                } else {
                    println("Connection inactive for user $userId")
                    failedConnections.add(connection)
                }
            } catch (e: Exception) {
                println("Error sending message to user $userId: ${e.message}")
                e.printStackTrace()
                failedConnections.add(connection)
            }
        }

        // Remove failed connections
        if (failedConnections.isNotEmpty()) {
            connections[userId]?.removeAll(failedConnections.toSet())
        }
    }

    // Method to handle chat room updates from external sources
    suspend fun handleChatRoomUpdate(
        chatRoom: PersonalChatRoom,
        paginationRequest: PaginationRequest
    ) {
        println("Handling chat room update: ${chatRoom.id} between ${chatRoom.userId} and ${chatRoom.friendId}")

        // Update both users involved in the chat
        val affectedUsers = listOf(chatRoom.userId, chatRoom.friendId)

        affectedUsers.forEach { userId ->
            if (connections.containsKey(userId)) {
                try {
                    val userChatRooms = repository.getInitialPersonalChats(userId, paginationRequest)
                    broadcastToUser(
                        userId,
                        WebSocketEventSendingDataType.UPDATE_DATA,
                        ChatRoomsUpdateData(userChatRooms)
                    )
                } catch (e: Exception) {
                    println("Error updating chat rooms for user $userId: ${e.message}")
                }
            }
        }
    }

    // Method to handle new messages (this would trigger chat room updates)
    suspend fun handleNewMessage(
        chatRoom: PersonalChatRoom,
        paginationRequest: PaginationRequest
    ) {
        println("Handling new message in chat room: ${chatRoom.id}")
        handleChatRoomUpdate(chatRoom, paginationRequest)
    }

    fun getTotalConnections(): Int = connections.values.sumOf { it.size }

    fun getConnectedUsers(): Set<String> = connections.keys.toSet()

//    suspend fun broadcastToAllUsers(messageType: String, data: ChatRoomsUpdateData) {
//        val message = WebSocketMessage(type = WebSocketEventSendingDataType.UPDATE_DATA, data = data)
//        val messageJson = json.encodeToString(message)
//
//        connections.values.flatten().forEach { connection ->
//            try {
//                if (connection.session.isActive) {
//                    connection.session.send(Frame.Text(messageJson))
//                }
//            } catch (e: Exception) {
//                println("Error broadcasting to user ${connection.userId}: ${e.message}")
//            }
//        }
//    }
//
//    suspend fun cleanup() {
//        println("Cleaning up connection manager")
//        userWatchJobs.values.forEach { it.cancel() }
//        userWatchJobs.clear()
//        connections.clear()
//        scope.cancel()
//    }
//
//    // Method to manually trigger updates (useful for testing)
//    suspend fun triggerUpdateForUser(userId: String) {
//        try {
//            val chatRooms = repository.getInitialPersonalChats(userId)
//            broadcastToUser(userId, WebSocketEventSendingDataType.TRIGGER_EVENT, ChatRoomsUpdateData(chatRooms))
//        } catch (e: Exception) {
//            println("Error triggering manual update for user $userId: ${e.message}")
//        }
//    }

    // Health check methods
    fun getConnectionInfo(): Map<String, Any> {
        return mapOf(
            "totalConnections" to getTotalConnections(),
            "connectedUsers" to getConnectedUsers(),
            "activeWatchJobs" to userWatchJobs.keys,
            "connectionsPerUser" to connections.mapValues { it.value.size }
        )
    }
}