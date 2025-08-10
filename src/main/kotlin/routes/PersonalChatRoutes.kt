/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:40 AM
 * Created by: ayaan
 *
 */

package com.aatech.routes

import com.aatech.config.api_config.PersonalChatRoutes
import com.aatech.config.api_config.checkAuth
import com.aatech.config.response.createErrorResponse
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.mongodb.repository.impl.PersonChatRepositoryImp
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.personalChatRoutes() {
    authenticate("auth-bearer") {
        post(PersonalChatRoutes.CreateChat.path) {
            checkAuth { authParam ->
                val personChatRepository: PersonChatRepository = PersonChatRepositoryImp()
                val userId = authParam.userId
                val chatModel = call.receive<PersonalChatRoom>()
                if (chatModel.userId != userId) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = createErrorResponse(
                            message = "Bad Request",
                            code = HttpStatusCode.BadRequest.value,
                            details = "User ID in chat model does not match authenticated user ID."
                        )
                    )
                    return@checkAuth
                }
                val chatId = personChatRepository.createChat(
                    model = chatModel.copy(
                        userId = userId
                    )
                )
                call.respond(
                    status = HttpStatusCode.Created,
                    message = chatId
                )
            }
        }
    }
}