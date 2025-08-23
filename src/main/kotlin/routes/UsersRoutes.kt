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
import com.aatech.dagger.components.DaggerFCMComponent
import com.aatech.dagger.modules.MySqlModule
import com.aatech.database.mysql.model.FriendshipRequestStatus
import com.aatech.database.mysql.model.entity.ChangeFriendRequestStatusBody
import com.aatech.database.mysql.model.entity.SendFriendRequestBody
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import com.aatech.database.mysql.repository.user.UserLogInRepository
import com.aatech.fcm.NotificationBuilder
import com.aatech.fcm.SendMessageService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

fun Routing.allUsersRoutes() {
    val userRepository = MySqlModule().provideUserInteractionRepository()
    val userLogInRepository = MySqlModule().provideUserLogInRepository()
    val fcmModule = DaggerFCMComponent.create().getSendMessageService()

    findFriends(
        repository = userRepository,
        userLogInRepository = userLogInRepository,
    )
    getAllFriends(repository = userRepository, userLogInRepository = userLogInRepository)
    sendFriendRequest(
        repository = userRepository,
        userLogInRepository = userLogInRepository,
        fcmModule = fcmModule
    )
    getAllReceivedRequests(repository = userRepository, userLogInRepository = userLogInRepository)
    getAllSentRequests(repository = userRepository, userLogInRepository = userLogInRepository)
    respondToFriendRequest(
        repository = userRepository,
        userLogInRepository = userLogInRepository,
        fcmModule = fcmModule
    )
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

fun Routing.sendFriendRequest(
    repository: UserInteractionRepository,
    userLogInRepository: UserLogInRepository,
    fcmModule: SendMessageService
) {
    authenticate("auth-bearer") {
        post(UserRoutes.AddOrRemoveFriendRequest.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                val body: SendFriendRequestBody = call.receive()
                if (body.requesterId.isBlank() || body.receiverId.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Bad Request",
                            details = "Requester ID or Receiver ID is missing."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                if (body.requesterId != authParams.userId) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        createErrorResponse(
                            code = HttpStatusCode.Forbidden.value,
                            message = "Forbidden",
                            details = "You are not allowed to send friend requests on behalf of another user."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val isRequestSent = repository.sendOrUnsendFriendRequest(
                        userId = body.requesterId,
                        friendId = body.receiverId
                    )
                    val message = when (isRequestSent) {
                        UserInteractionRepository.FriendshipAction.SEND -> "Friend request sent successfully."
                        UserInteractionRepository.FriendshipAction.UNSEND -> "Friend request unsent successfully."
                    }
                    if (isRequestSent == UserInteractionRepository.FriendshipAction.SEND) {
                        val userDetails = repository.getUserById(body.requesterId)
                            ?: throw IllegalArgumentException("User details not found for ID: ${body.receiverId}")
                        val userToken = repository.getUserTokenById(body.receiverId)
                            ?: throw IllegalArgumentException("User details not found for ID: ${body.requesterId}")
                        fcmModule.sendMessage(
                            NotificationBuilder()
                                .to(userToken)
                                .setSendFriendRequestData {
                                    title("New Friend Request")
                                    body("${userDetails.username} sent you a friend request. 🤝")
                                    senderId(body.requesterId)
                                    receiverId(body.receiverId)
                                    senderName(userDetails.username)
                                    senderImage(userDetails.profilePicture ?: "")
                                    notificationId(body.requesterId.hashCode())
                                }
                                .buildToMessage()
                        )
                    } else {
                        val userToken = repository.getUserTokenById(body.receiverId)
                            ?: throw IllegalArgumentException("User details not found for ID: ${body.requesterId}")
                        fcmModule.sendMessage(
                            NotificationBuilder()
                                .to(userToken)
                                .setCancelNotificationData {
                                    notificationId(body.requesterId.hashCode().toString())
                                }
                                .buildToMessage()
                        )
                    }
                    call.respond(HttpStatusCode.Created, message)
                } catch (_: ExposedSQLException) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        createErrorResponse(
                            code = HttpStatusCode.Conflict.value,
                            message = "Conflict",
                            details = "Friend request already exists."
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = "An error occurred while sending the friend request: ${e.message}"
                        )
                    )
                }
            }
        }
    }
}


fun Routing.getAllFriends(
    repository: UserInteractionRepository,
    userLogInRepository: UserLogInRepository
) {
    authenticate("auth-bearer") {
        get(UserRoutes.GetFriendsList.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                try {
                    val friends = repository.getAllFriends(
                        userId = authParams.userId ?: throw IllegalArgumentException("User ID is required")
                    )
                    call.respond(HttpStatusCode.OK, friends)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = "An error occurred while fetching friends list: ${e.message}"
                        )
                    )
                }
            }
        }
    }
}

fun Routing.getAllReceivedRequests(
    repository: UserInteractionRepository,
    userLogInRepository: UserLogInRepository
) {
    authenticate("auth-bearer") {
        get(UserRoutes.GetAllReceivedRequests.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                try {
                    val requests = repository.getAllReceivedRequests(
                        userId = authParams.userId ?: throw IllegalArgumentException("User ID is required")
                    )
                    call.respond(HttpStatusCode.OK, requests)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = "An error occurred while fetching received requests: ${e.message}"
                        )
                    )
                }
            }
        }
    }
}

fun Routing.getAllSentRequests(
    repository: UserInteractionRepository,
    userLogInRepository: UserLogInRepository
) {
    authenticate("auth-bearer") {
        get(UserRoutes.GetAllSentRequests.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                try {
                    val requests = repository.getAllSentRequests(
                        userId = authParams.userId ?: throw IllegalArgumentException("User ID is required")
                    )
                    call.respond(HttpStatusCode.OK, requests)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = "An error occurred while fetching sent requests: ${e.message}"
                        )
                    )
                }
            }
        }
    }
}

fun Routing.respondToFriendRequest(
    repository: UserInteractionRepository,
    userLogInRepository: UserLogInRepository,
    fcmModule: SendMessageService
) {
    authenticate("auth-bearer") {
        post(UserRoutes.RespondToFriendRequest.path) {
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository
            ) { authParams ->
                val body = call.receive<ChangeFriendRequestStatusBody>()
                if (body.userId.isBlank() || body.friendId.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Bad Request",
                            details = "Requester ID or Receiver ID is missing."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                if (body.userId != authParams.userId) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        createErrorResponse(
                            code = HttpStatusCode.Forbidden.value,
                            message = "Forbidden",
                            details = "You are not allowed to respond to friend requests on behalf of another user."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val responseMessage = repository.responseToFriendRequest(
                        userId = body.userId,
                        friendId = body.friendId,
                        requestStatus = body.requestStatus
                    )
                    val friendsToken = repository.getUserTokenById(body.friendId)
                    if (body.requestStatus == FriendshipRequestStatus.ACCEPTED) {
                        if (friendsToken != null) {
                            val userDetails = repository.getUserById(body.userId)
                                ?: throw IllegalArgumentException("User details not found for ID: ${body.userId}")
                            fcmModule.sendMessage(
                                NotificationBuilder()
                                    .to(friendsToken)
                                    .setSendFriendRequestData {
                                        title("Friend Request Accepted")
                                        body("${userDetails.username} accepted your friend request 🥂.")
                                        senderId(body.userId)
                                        receiverId(body.friendId)
                                        senderName(userDetails.username)
                                        senderImage(userDetails.profilePicture ?: "")
                                    }
                                    .buildToMessage()
                            )
                        }
                    }
                    call.respond(HttpStatusCode.OK, responseMessage)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        createErrorResponse(
                            code = HttpStatusCode.InternalServerError.value,
                            message = "Internal Server Error",
                            details = "An error occurred while responding to the friend request: ${e.message}"
                        )
                    )
                }
            }
        }
    }
}