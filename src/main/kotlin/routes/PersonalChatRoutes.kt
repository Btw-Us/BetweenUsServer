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
import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.components.DaggerMongoDbComponent
import com.aatech.dagger.modules.MySqlModule
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.database.mongodb.repository.impl.PersonChatRepositoryImp
import com.aatech.database.mysql.repository.user.UserLogInRepository
import com.aatech.database.utils.PaginationRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json


fun Routing.allPersonalChatRoutes() {
    val userLogInRepository = MySqlModule().provideUserLogInRepository()
    getPersonalChatsRoute(
        userLogInRepository = userLogInRepository
    )
    personalChatRoutes(
        userLogInRepository = userLogInRepository
    )
}


fun Routing.getPersonalChatsRoute(
    userLogInRepository: UserLogInRepository,
) {
    authenticate("auth-bearer") {
        webSocket("${PersonalChatRoutes.GetAllChats.path}/{userId}") {
            val userId = call.parameters["userId"]
            val initialPage = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val initialPageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            checkDeviceIntegrity(
                currentUserId = userId,
                userLogInRepository = userLogInRepository
            ) {
                if (userId.isNullOrBlank()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "User ID is required"))
                    return@checkDeviceIntegrity
                }

                val connectionManager = DaggerMongoDbComponent.create().getPersonChatRoomConnectionManager()

                // Add connection with initial pagination
                connectionManager.addConnection(
                    userId,
                    paginationRequest = PaginationRequest(
                        page = initialPage,
                        pageSize = initialPageSize
                    ),
                    this
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
    userLogInRepository: UserLogInRepository
) {
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