/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 20, 2025 08:19 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

class SendMessageService(
    private val fcm: FirebaseMessaging
) {
    fun sendMessage(message: Message): String {
        return try {
            val response = fcm.send(message)
            println("Successfully sent message: $response")
            response
        } catch (e: Exception) {
            println("Error sending message: ${e.message}")
            throw e
        }
    }
}