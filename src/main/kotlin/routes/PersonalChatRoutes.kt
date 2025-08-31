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
import com.aatech.config.api_config.checkDeviceIntegrity
import com.aatech.config.body.CreatePersonalChatRoomRequest
import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.components.DaggerMongoDbComponent
import com.aatech.dagger.components.DaggerMySqlComponent
import com.aatech.dagger.components.DaggerWebSocketComponent
import com.aatech.database.mongodb.model.Message
import com.aatech.database.mongodb.model.MessageState
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.mysql.repository.user.UserLogInRepository
import com.aatech.database.usecase.CreateChatRoomUseCase
import com.aatech.database.utils.PaginationRequest
import com.aatech.database.utils.TransactionResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking


fun Routing.allPersonalChatRoutes() {
    val userLogInRepository = DaggerMySqlComponent.create().getUserRepository()
    val personalChatRepository = DaggerMongoDbComponent.create().getPersonChatRepository()
    val createChatRoomUseCase = CreateChatRoomUseCase(
        personalChatRepository = personalChatRepository,
        userInteraction = DaggerMySqlComponent.create().getUserInteractionRepository()
    )
    watchPersonalChats(
        userLogInRepository = userLogInRepository
    )
    createPersonalChatRoomRoute(
        createChatRoomUseCase = createChatRoomUseCase,
        userLogInRepository = userLogInRepository
    )
    getChats(
        personalChatRepository
    )
    getAllMessages(
        userLogInRepository = userLogInRepository,
        personalChatRepository = personalChatRepository
    )
    sendNewMessage(
        userLogInRepository = userLogInRepository,
        personalChatRepository = personalChatRepository
    )
}


fun Routing.getChats(personalChatRepository: PersonChatRepository) {
    authenticate("auth-bearer") {
        get("${PersonalChatRoutes.GetChats.path}/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            if (userId.isBlank()) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = createErrorResponse(
                        message = "Bad Request",
                        code = HttpStatusCode.BadRequest.value,
                        details = "User ID is required."
                    )
                )
                return@get
            }
            checkAuth { authParam ->
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                if (authParam.userId != userId) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            message = "Bad Request",
                            code = HttpStatusCode.BadRequest.value,
                            details = "User ID in query parameter does not match authenticated user ID."
                        )
                    )
                    return@checkAuth
                }
                try {
                    val paginatedChats = runBlocking {
                        personalChatRepository.getAllPersonalChatRoom(
                            userID = userId, paginationRequest = PaginationRequest(
                                page = page, pageSize = pageSize
                            )
                        )
                    }
                    call.respond(
                        status = HttpStatusCode.OK, message = paginatedChats
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError, message = createErrorResponse(
                            message = "Internal Server Error",
                            code = HttpStatusCode.InternalServerError.value,
                            details = e.message ?: "An unexpected error occurred."
                        )
                    )
                }
            }
        }
    }
}


fun Routing.watchPersonalChats(
    userLogInRepository: UserLogInRepository,
) {
    authenticate("auth-bearer") {
        webSocket("${PersonalChatRoutes.WatchPersonalChats.path}/{userId}") {
            val userId = call.parameters["userId"]
            checkDeviceIntegrity(
                currentUserId = userId, userLogInRepository = userLogInRepository
            ) {
                if (userId.isNullOrBlank()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "User ID is required"))
                    return@checkDeviceIntegrity
                }

                val connectionManager = DaggerWebSocketComponent.create().provideWebSocketConnectionManager()

                connectionManager.addConnection(
                    userId, this
                )

                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {

                            }

                            is Frame.Close -> {
                                println("WebSocket connection closed for user $userId")
                                break
                            }

                            else -> {
                                println("Received unexpected frame type from user $userId")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error in WebSocket connection for user $userId: ${e.message}")
                    e.printStackTrace()
                } finally {
                    connectionManager.removeConnection(userId)
                    println("Connection removed for user $userId")
                }
            }
        }
    }
}


fun Routing.createPersonalChatRoomRoute(
    userLogInRepository: UserLogInRepository,
    createChatRoomUseCase: CreateChatRoomUseCase
) {
    authenticate("auth-bearer") {
        post(PersonalChatRoutes.CreateChat.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParam ->
                val userId = authParam.userId
                val chatModel = call.receive<CreatePersonalChatRoomRequest>()
                if (chatModel.userId != userId) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            message = "Bad Request",
                            code = HttpStatusCode.BadRequest.value,
                            details = "User ID in chat model does not match authenticated user ID."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val chatId = createChatRoomUseCase(
                        userId = chatModel.userId, friendsId = chatModel.friendsId, message = chatModel.message
                    )
                    when (chatId) {
                        is TransactionResult.Failure<*, *> -> {
                            val exception = chatId.exception
                            call.respond(
                                status = HttpStatusCode.InternalServerError, message = createErrorResponse(
                                    message = "Transaction Failed: ${exception.message}",
                                    code = HttpStatusCode.InternalServerError.value,
                                    details = "An error occurred while creating the chat room."
                                )
                            )
                        }

                        is TransactionResult.Success<*, *> -> {
                            call.respond(
                                status = HttpStatusCode.OK, message = mapOf("chatRoomId" to chatId.mongoResult)
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError, message = createErrorResponse(
                            message = "Internal Server Error",
                            code = HttpStatusCode.InternalServerError.value,
                            details = e.message ?: "An unexpected error occurred."
                        )
                    )
                }
            }
        }
    }
}


fun Routing.sendNewMessage(
    userLogInRepository: UserLogInRepository,
    personalChatRepository: PersonChatRepository
) {
    authenticate("auth-bearer") {
        post(PersonalChatRoutes.SendMessage.path) {
            val roomId = getPersonalChatRoomIdFromCall(call)
            if (roomId.isNullOrBlank()) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = createErrorResponse(
                        message = "Bad Request",
                        code = HttpStatusCode.BadRequest.value,
                        details = "Personal Chat Room ID is required."
                    )
                )
                return@post
            }
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { _ ->
                val messageModel = call.receive<Message>()
                if (messageModel.chatRoomId != roomId) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            message = "Bad Request",
                            code = HttpStatusCode.BadRequest.value,
                            details = "Chat Room ID in message model does not match the URL parameter."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val messageId = runBlocking {
                        personalChatRepository.addChatEntry(
                            model = messageModel.copy(
                                messageState = MessageState.SEND
                            )
                        )
                    }
                    call.respond(
                        message = messageId,
                        status = HttpStatusCode.Created
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError, message = createErrorResponse(
                            message = "Internal Server Error",
                            code = HttpStatusCode.InternalServerError.value,
                            details = e.message ?: "An unexpected error occurred."
                        )
                    )
                }
            }
        }
    }
}


private fun getPersonalChatRoomIdFromCall(call: ApplicationCall): String? {
    return call.parameters["personalChatRoomId"]
}

fun Routing.getAllMessages(
    userLogInRepository: UserLogInRepository,
    personalChatRepository: PersonChatRepository
) {
    authenticate("auth-bearer") {
        get(PersonalChatRoutes.GetAllMessages.path) {
            val personalChatRoomId = getPersonalChatRoomIdFromCall(call)
            if (personalChatRoomId.isNullOrBlank()) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = createErrorResponse(
                        message = "Bad Request",
                        code = HttpStatusCode.BadRequest.value,
                        details = "Personal Chat Room ID is required."
                    )
                )
                return@get
            }
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { _ ->
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                try {
                    val paginatedMessages = runBlocking {
                        personalChatRepository.getChatEntries(
                            personalChatRoomId = personalChatRoomId, paginationRequest = PaginationRequest(
                                page = page, pageSize = pageSize
                            )
                        )
                    }
                    call.respond(
                        status = HttpStatusCode.OK, message = paginatedMessages
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError, message = createErrorResponse(
                            message = "Internal Server Error",
                            code = HttpStatusCode.InternalServerError.value,
                            details = e.message ?: "An unexpected error occurred."
                        )
                    )
                }
            }
        }
    }
}