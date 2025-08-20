/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 20, 2025 07:55 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.fcm

import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationModel(
    val message: MessageSend
)

@Serializable
data class MessageSend(
    val topic: String? = null,
    val to: String? = null,
    val notification: NotificationSend,
    val data: NotificationData? = null,
)

@Serializable
data class NotificationSend(
    val title: String,
    val body: String,
)

@Serializable
sealed interface NotificationData


@Serializable
enum class SendOrAcceptFriendRequestType {
    SEND, ACCEPT
}

@Serializable
@SerialName("SendOrAcceptFriendRequest")
data class SendOrAcceptFriendRequest(
    val senderId: String,
    val receiverId: String,
    val senderName: String,
    val senderImage: String,
    val actionType: SendOrAcceptFriendRequestType = SendOrAcceptFriendRequestType.SEND
) : NotificationData {

    fun putData(): Map<String, String> {
        return mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "senderName" to senderName,
            "senderImage" to senderImage,
            "type" to actionType.name
        )
    }
}


fun NotificationModel.toMessage(): Message =
    Message.builder()
        .setNotification(
            Notification.builder()
                .setTitle(message.notification.title)
                .setBody(message.notification.body)
                .build()
        ).apply {
            if (message.to != null)
                setToken(message.to)
            else
                setTopic(message.topic)
        }
        .apply {
            with(message.data) {
                if (this != null) {
                    when (this) {
                        is SendOrAcceptFriendRequest ->
                            this.putData()
                    }
                }
            }
        }
        .build()
