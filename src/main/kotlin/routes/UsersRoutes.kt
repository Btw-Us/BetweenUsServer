/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 13, 2025 08:54 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.routes

import com.aatech.config.api_config.UserRoutes
import com.aatech.config.api_config.checkDeviceIntegrity
import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.modules.MySqlModule
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import com.aatech.database.mysql.repository.user.UserLogInRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.allUsersRoutes() {
    val userRepository = MySqlModule().provideUserInteractionRepository()
    val userLogInRepository = MySqlModule().provideUserLogInRepository()
    findFriends(userRepository, userLogInRepository)
}

fun Routing.findFriends(
    repository: UserInteractionRepository,
    userLogInRepository: UserLogInRepository
) {
    authenticate("auth-bearer") {
        get(UserRoutes.FindFriends.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                val userName = call.parameters["userNameQuery"] ?: ""
                if (userName.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Bad Request",
                            details = "Username parameter is missing or invalid."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val searchItems = repository.findFriends(
                        loggedUserId = authParams.userId ?: throw IllegalArgumentException("User ID is required"),
                        userName = userName
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        searchItems
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = "An error occurred while searching for friends: ${e.message}"
                        )
                    )
                    return@checkDeviceIntegrity
                }
            }
        }
    }
}