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
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.mysql.repository.user.UserLogInRepository
import com.aatech.database.usecase.CreateChatRoomUseCase
import com.aatech.database.utils.PaginationRequest
import com.aatech.database.utils.TransactionResult
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json


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
    personalChatRoutes(
        createChatRoomUseCase = createChatRoomUseCase
    )
    getChats(
        personalChatRepository
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
            val initialPage = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val initialPageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            checkDeviceIntegrity(
                currentUserId = userId, userLogInRepository = userLogInRepository
            ) {
                if (userId.isNullOrBlank()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "User ID is required"))
                    return@checkDeviceIntegrity
                }

                val connectionManager = DaggerMongoDbComponent.create().getPersonChatRoomConnectionManager()

                // Add connection with initial pagination
                connectionManager.addConnection(
                    userId, paginationRequest = PaginationRequest(
                        page = initialPage, pageSize = initialPageSize
                    ), this
                )

                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                try {
                                    val messageText = frame.readText()
                                    println("Received message from user $userId: $messageText")

                                    // Parse incoming pagination request
                                    val json = Json { ignoreUnknownKeys = true }
                                    val paginationRequest = json.decodeFromString<PaginationRequest>(messageText)

                                    println("Updating pagination for user $userId - Page: ${paginationRequest.page}, PageSize: ${paginationRequest.pageSize}")


                                    connectionManager.updateConnectionPagination(userId, paginationRequest, this)

                                } catch (e: Exception) {
                                    println("Error parsing pagination request from user $userId: ${e.message}")
                                    e.printStackTrace()

                                    // Send error response back to client
                                    send(Frame.Text("""{"error": "Invalid pagination request format"}"""))
                                }
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
                    connectionManager.removeConnection(userId, this)
                    println("Connection removed for user $userId")
                }
            }
        }
    }
}


fun Routing.personalChatRoutes(
    createChatRoomUseCase: CreateChatRoomUseCase
) {
    authenticate("auth-bearer") {
        post(PersonalChatRoutes.CreateChat.path) {
            checkAuth { authParam ->
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
                    return@checkAuth
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