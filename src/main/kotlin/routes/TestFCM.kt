/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 20, 2025 08:47 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.routes

import com.aatech.config.api_config.BASE_REST_PATH
import com.aatech.dagger.components.DaggerFCMComponent
import com.aatech.fcm.NotificationModel
import com.aatech.fcm.toMessage
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.sendTestNotification() {
    post("$BASE_REST_PATH/test-fcm") {
        val fcmModule = DaggerFCMComponent.create().getSendMessageService()
        try {
            val notificationModel = call.receive<NotificationModel>()
            fcmModule.sendMessage(
                notificationModel.toMessage()
            )
            call.respond(
                status = HttpStatusCode.OK,
                message = notificationModel,
            )
        } catch (e: Exception) {
            println("Error receiving notification model: ${e.message}")
            call.respondText(
                "Error receiving notification model: ${e.message}",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
        }
    }
}