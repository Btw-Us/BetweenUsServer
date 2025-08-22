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
    val actionType: SendOrAcceptFriendRequestType = SendOrAcceptFriendRequestType.SEND,
) : NotificationData


fun NotificationModel.toMessage(): Message =
    Message.builder()
        .setNotification(
            Notification.builder()
                .setTitle(message.notification.title)
                .setBody(message.notification.body)
                .setImage(
                    if (message.data == SendOrAcceptFriendRequest)
                        (message.data as SendOrAcceptFriendRequest).senderImage
                    else null
                )
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
                        is SendOrAcceptFriendRequest -> {
                            putData("type", "SendOrAcceptFriendRequest")
                            putData("senderId", senderId)
                            putData("receiverId", receiverId)
                            putData("senderName", senderName)
                            putData("senderImage", senderImage)
                            putData("actionType", actionType.name)
                        }
                    }
                }
            }
        }
        .build()

class NotificationBuilder {
    private var userToken: String? = null
    private var topic: String? = null
    private var title: String = ""
    private var body: String = ""
    private var data: NotificationData? = null

    fun to(token: String) = apply { this.userToken = token }
    fun topic(topic: String) = apply { this.topic = topic }
    fun title(title: String) = apply { this.title = title }
    fun body(body: String) = apply { this.body = body }
    fun data(data: NotificationData) = apply { this.data = data }

    fun setData(senderId: String, receiverId: String, senderName: String, senderImage: String = "") = apply {
        this.data = SendOrAcceptFriendRequest(
            senderId = senderId,
            receiverId = receiverId,
            senderName = senderName,
            senderImage = senderImage,
            actionType = SendOrAcceptFriendRequestType.SEND
        )
    }

    fun build(): NotificationModel {
        return NotificationModel(
            message = MessageSend(
                to = userToken,
                topic = topic,
                notification = NotificationSend(title = title, body = body),
                data = data
            )
        )
    }
}

fun notification() = NotificationBuilder()