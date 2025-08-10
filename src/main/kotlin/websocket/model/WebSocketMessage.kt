/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 10, 2025 04:16 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.websocket.model

import com.aatech.database.mongodb.model.PersonalChatRoom
import io.ktor.websocket.WebSocketSession
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessage<T>(
    val type: String,
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ChatRoomsUpdateData(
    val chatRooms: List<PersonalChatRoom>
)

data class PersonalChatConnection(
    val session: WebSocketSession,
    val userId: String,
    val connectedAt: Long = System.currentTimeMillis()
)