/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: September 01, 2025 12:29 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.websocket.cm

import com.aatech.database.mongodb.model.PersonalChatChangeEvent
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.websocket.model.WebSocketMessage
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class PersonalChatConnectionManager @Inject constructor(
    private val personalChatRepository: PersonChatRepository,
    private val json: Json
) {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun addConnection(
        userId: String,
        session: WebSocketSession
    ) {
        connections[userId] = session
        session.send(
            sendData(
                PersonalChatChangeEvent.Connected()
            )
        )
        observePersonalChatChanges(userId, session)
    }

    private fun observePersonalChatChanges(
        userId: String,
        session: WebSocketSession
    ) {
        scope.launch {
            personalChatRepository.watchPersonalChats(userId)
                .collect { event ->
                    try {
                        if (session.isActive) {
                            session.send(
                                sendData(
                                    event
                                )
                            )
                        } else {
                            connections.remove(userId)
                        }
                    } catch (_: Exception) {
                        connections.remove(userId)
                    }
                }
        }
    }

    private fun sendData(
        event: PersonalChatChangeEvent,
    ): Frame.Text = Frame.Text(
        json.encodeToString(
            WebSocketMessage(
                data = event
            )
        )
    )

    fun removeConnection(userId: String) {
        connections.remove(userId)
    }
}