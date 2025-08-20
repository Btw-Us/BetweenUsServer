/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 21, 2025 01:10 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.routes

import com.aatech.config.api_config.FirebaseMessagingRoutes
import com.aatech.config.api_config.checkDeviceIntegrity
import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.modules.MySqlModule
import com.aatech.database.mysql.model.entity.UserFirebaseToken
import com.aatech.database.mysql.model.entity.UserNotificationToken
import com.aatech.database.mysql.repository.user.UserLogInRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.firebaseMessagingRoutes() {
    val userLogInRepository = MySqlModule().provideUserLogInRepository()
    createOrUpdateFirebaseMessagingToken(
        userLogInRepository = userLogInRepository
    )
}


fun Routing.createOrUpdateFirebaseMessagingToken(userLogInRepository: UserLogInRepository) {
    authenticate("auth-bearer") {
        post(FirebaseMessagingRoutes.AddOrUpdateNotificationToken.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                val userFirebaseToken: UserFirebaseToken = call.receive()
                if (userFirebaseToken.token.isBlank() || userFirebaseToken.userId.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Bad Request",
                            details = "Token or User ID cannot be blank."
                        )
                    )
                    return@checkDeviceIntegrity
                }

                if (authParams.userId != userFirebaseToken.userId) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        createErrorResponse(
                            code = HttpStatusCode.Unauthorized.value,
                            message = "Unauthorized",
                            details = "User ID does not match the authenticated user."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    userLogInRepository.addOrUpdateUserNotificationToken(
                        userId = userFirebaseToken.userId,
                        tokenModel = UserNotificationToken(
                            userId = userFirebaseToken.userId,
                            token = userFirebaseToken.token
                        )
                    )
                    call.respond(HttpStatusCode.OK, "Token updated successfully.")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = e.message ?: "An error occurred while updating the token."
                        )
                    )
                }
            }
        }
    }
}