/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: September 13, 2025 03:18 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.websocket.cm

import com.aatech.database.mongodb.model.MessageChangeEvent
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.websocket.collectForWebSocket
import com.aatech.websocket.model.WebSocketMessage
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class AllMessageConnectionManager @Inject constructor(
    private val personalChatRepository: PersonChatRepository,
    private val json: Json
) {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun addConnection(
        personalChatRoomId: String,
        session: WebSocketSession
    ) {
        connections[personalChatRoomId] = session
        session.send(
            sendData(
                MessageChangeEvent.Connected()
            )
        )
        observeMessageChanges(personalChatRoomId, session)
    }

    private fun observeMessageChanges(
        chatRoomId: String,
        session: WebSocketSession
    ) {
        scope.launch {
            personalChatRepository.watchChatEntries(chatRoomId)
                .collectForWebSocket(
                    id = chatRoomId,
                    scope = this,
                    session = session,
                    connections = connections,
                    sendData = ::sendData
                )
        }
    }


    private fun sendData(
        event: MessageChangeEvent,
    ): Frame.Text = Frame.Text(
        json.encodeToString(
            WebSocketMessage(
                data = event
            )
        )
    )

    fun removeConnection(personalChatRoomId: String) {
        connections.remove(personalChatRoomId)
    }
}
