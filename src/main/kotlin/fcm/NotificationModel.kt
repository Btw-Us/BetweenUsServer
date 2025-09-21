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
    val data: NotificationData? = null,
)

@Serializable
sealed interface NotificationData {
    val notificationId: String
    val title: String
    val body: String
}

@Serializable
enum class SendOrAcceptFriendRequestType {
    SEND, ACCEPT
}

@Serializable
@SerialName("SendOrAcceptFriendRequest")
data class SendOrAcceptFriendRequestNotificationData(
    override val title: String,
    override val body: String,
    val senderId: String,
    val receiverId: String,
    val senderName: String,
    val senderImage: String,
    val actionType: SendOrAcceptFriendRequestType = SendOrAcceptFriendRequestType.SEND,
    override val notificationId: String,
) : NotificationData

@Serializable
@SerialName("CancelNotification")
data class CancelNotificationData(
    override val notificationId: String,
    override val title: String,
    override val body: String
) : NotificationData


@Serializable
@SerialName("MessageNotification")
data class MessageNotificationData(
    override val notificationId: String,
    override val title: String,
    override val body: String,
    val senderId: String,
    val receiverId: String,
    val senderName: String,
    val senderImage: String,
    val messageId: String,
    val chatRoomId: String,
) : NotificationData

fun NotificationModel.toMessage(): Message = Message.builder().apply {
    if (message.to != null) setToken(message.to)
    else setTopic(message.topic)
}.apply {
    with(message.data) {
        if (this != null) {
            when (this) {
                is SendOrAcceptFriendRequestNotificationData -> {
                    putData("type", "SendOrAcceptFriendRequest")
                    putData("title", title)
                    putData("body", body)
                    putData("senderId", senderId)
                    putData("receiverId", receiverId)
                    putData("senderName", senderName)
                    putData("senderImage", senderImage)
                    putData("actionType", actionType.name)
                    putData("notificationId", notificationId)
                }

                is CancelNotificationData -> {
                    putData("type", "CancelNotification")
                    putData("notificationId", notificationId)
                }

                is MessageNotificationData -> {
                    putData("type", "MessageNotification")
                    putData("title", title)
                    putData("body", body)
                    putData("senderId", senderId)
                    putData("receiverId", receiverId)
                    putData("senderName", senderName)
                    putData("senderImage", senderImage)
                    putData("messageId", messageId)
                    putData("chatRoomId", chatRoomId)
                    putData("notificationId", notificationId)
                }
            }
        }
    }
}.build()

class NotificationBuilder {
    private var userToken: String? = null
    private var topicOrToken: String? = null
    private var data: NotificationData? = null

    fun to(token: String) = apply { this.userToken = token }
    fun topic(topic: String) = apply { this.topicOrToken = topic }
    fun data(data: NotificationData) = apply { this.data = data }

    class MessageNotificationBuilder {
        private var title: String = ""
        private var body: String = ""
        private var senderId: String = ""
        private var receiverId: String = ""
        private var senderName: String = ""
        private var senderImage: String = ""
        private var messageId: String = ""
        private var chatRoomId: String = ""
        private var notificationId: Int = chatRoomId.hashCode()


        fun title(title: String) = apply { this.title = title }
        fun body(body: String) = apply { this.body = body }
        fun senderId(senderId: String) = apply { this.senderId = senderId }
        fun receiverId(receiverId: String) = apply { this.receiverId = receiverId }
        fun senderName(senderName: String) = apply { this.senderName = senderName }
        fun senderImage(senderImage: String) = apply { this.senderImage = senderImage }
        fun messageId(messageId: String) = apply { this.messageId = messageId }


        fun buildMisidentificationData() = MessageNotificationData(
            title = title,
            body = body,
            senderId = senderId,
            receiverId = receiverId,
            senderName = senderName,
            senderImage = senderImage,
            messageId = messageId,
            chatRoomId = chatRoomId,
            notificationId = notificationId.toString()
        )
    }



    class SendFriendRequestBuilder {
        private var title: String = ""
        private var body: String = ""
        private var senderId: String = ""
        private var receiverId: String = ""
        private var senderName: String = ""
        private var senderImage: String = ""
        private var notificationId: Int = (0..100000).random()
        private var actionType: SendOrAcceptFriendRequestType = SendOrAcceptFriendRequestType.SEND


        fun title(title: String) = apply { this.title = title }
        fun body(body: String) = apply { this.body = body }
        fun senderId(senderId: String) = apply { this.senderId = senderId }
        fun receiverId(receiverId: String) = apply { this.receiverId = receiverId }
        fun senderName(senderName: String) = apply { this.senderName = senderName }
        fun senderImage(senderImage: String) = apply { this.senderImage = senderImage }
        fun notificationId(notificationId: Int) = apply { this.notificationId = notificationId }
        fun actionType(actionType: SendOrAcceptFriendRequestType) = apply { this.actionType = actionType }

        fun buildSendFriendRequestData() = SendOrAcceptFriendRequestNotificationData(
            title = title,
            body = body,
            senderId = senderId,
            receiverId = receiverId,
            senderName = senderName,
            senderImage = senderImage,
            actionType = actionType,
            notificationId = notificationId.toString()
        )
    }

    class CancelNotificationBuilder {
        private var notificationId: String = ""

        fun notificationId(notificationId: String) = apply { this.notificationId = notificationId }

        fun buildCancelNotificationData() = CancelNotificationData(
            notificationId = notificationId,
            title = "",
            body = ""
        )
    }

    fun setSendFriendRequestData(block: SendFriendRequestBuilder.() -> Unit) = apply {
        this.data = SendFriendRequestBuilder().apply(block).buildSendFriendRequestData()
    }

    fun setCancelNotificationData(block: CancelNotificationBuilder.() -> Unit) = apply {
        this.data = CancelNotificationBuilder().apply(block).buildCancelNotificationData()
    }

    fun setMessageNotificationData(block: MessageNotificationBuilder.() -> Unit) = apply {
        this.data = MessageNotificationBuilder().apply(block).buildMisidentificationData()
    }


    fun build(): NotificationModel {
        if (userToken == null && topicOrToken == null) throw IllegalArgumentException("Either userToken or topic must be provided")
        if (data == null) throw IllegalArgumentException("Notification data must be provided")
        return NotificationModel(
            message = MessageSend(
                to = userToken, topic = topicOrToken, data = data
            )
        )
    }

    fun buildToMessage(): Message = build().toMessage()
}