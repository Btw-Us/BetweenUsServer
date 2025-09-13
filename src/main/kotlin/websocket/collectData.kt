/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: September 13, 2025 03:26 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.websocket

import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Collects data from the provided [Flow] and sends it to the specified [WebSocketSession].
 * If the session is inactive or an error occurs, the connection is removed from the [connections] map.
 *
 * @param id The identifier for the connection.
 * @param scope The coroutine scope to launch the collection in.
 * @param session The WebSocket session to send data to.
 * @param connections A mutable map of active connections.
 * @param sendData A lambda function that converts the collected data to a [Frame.Text] for sending.
 */
inline fun <T> Flow<T>.collectForWebSocket(
    id: String,
    scope: kotlinx.coroutines.CoroutineScope,
    session: WebSocketSession,
    connections: MutableMap<String, WebSocketSession>,
    crossinline sendData: (T) -> Frame.Text
) {
    scope.launch {
        collect { event ->
            try {
                if (session.isActive) {
                    session.send(sendData(event))
                } else {
                    connections.remove(id)
                }
            } catch (e: Exception) {
                connections.remove(id)
                session.close(CloseReason(INTERNAL_ERROR, "Error: ${e.message}"))
            }
        }
    }
}