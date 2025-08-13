/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: July 30, 2025 10:35 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.routes

import com.aatech.config.api_config.LoginRoutes
import com.aatech.config.api_config.checkDeviceIntegrity
import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.components.DaggerMySqlComponent
import com.aatech.database.mysql.model.entity.RegisterUserRequest
import com.aatech.database.mysql.model.entity.SetUpUserProfile
import com.aatech.database.mysql.model.entity.UserLogInResponse
import com.aatech.database.mysql.model.entity.toUserEntity
import com.aatech.database.mysql.repository.user.UserLogInRepository
import com.aatech.plugin.fetchUserInfo
import com.aatech.plugin.toUserEntity
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Routing.allLogInRoutes() {
    logInWithOAuth()
    logInWithGoogle()
    setUpUserProfile()
    checkUserNameAvailable()
    checkUserPassword()
}


fun Routing.logInWithGoogle() {
    authenticate("auth-bearer") {
        post(LoginRoutes.LogInWithGoogle.path) {
            val authTokenService: UserLogInRepository = DaggerMySqlComponent.create().getUserRepository()
            checkDeviceIntegrity(false) { authParam ->
                val user = call.receive<RegisterUserRequest>()
                try {
                    val loggedUser = authTokenService.createUser(
                        user.toUserEntity(),
                        deviceInfo = Pair(authParam.deviceId!!, authParam.deviceModel!!)
                    )
                    call.respond(
                        status = if (loggedUser.isProfileSetUpDone) HttpStatusCode.Created else HttpStatusCode.OK,
                        message = loggedUser
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Failed to log in with Google.",
                            details = e.message ?: "Unknown error occurred while logging in with Google."
                        )
                    )
                }
            }
        }
    }
}

fun Routing.setUpUserProfile() {
    authenticate("auth-bearer") {
        post(LoginRoutes.SetUpUserProfile.path) {
            val setUpUserProfile = call.receive<SetUpUserProfile>()
            val authTokenService: UserLogInRepository = DaggerMySqlComponent.create().getUserRepository()
            checkDeviceIntegrity(
                userLogInRepository = authTokenService,
            ) { authParam ->
                if (authParam.userId != setUpUserProfile.userId) {
                    call.respond(
                        status = HttpStatusCode.Unauthorized, message = createErrorResponse(
                            code = HttpStatusCode.Unauthorized.value,
                            message = "Unauthorized access. User ID mismatch.",
                            details = "The user ID in the request does not match the authenticated user ID."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                if (setUpUserProfile.passwordHash.isBlank()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Password cannot be empty.",
                            details = "Please provide a valid password to set up your profile."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val loggedUser = authTokenService.setUpProfile(setUpUserProfile)
                    call.respond(
                        status = HttpStatusCode.OK, message = loggedUser
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Failed to set up user profile.",
                            details = e.message ?: "Unknown error occurred while setting up user profile."
                        )
                    )
                }
            }
        }
    }
}


fun Routing.checkUserPassword() {
    authenticate("auth-bearer") {
        post(LoginRoutes.CheckPassword.path) {
            val setUpUserProfile = call.receive<SetUpUserProfile>()
            val userLogInRepository: UserLogInRepository = DaggerMySqlComponent.create().getUserRepository()
            checkDeviceIntegrity(
                userLogInRepository = userLogInRepository,
            ) { authParam ->
                if (authParam.userId != setUpUserProfile.userId) {
                    call.respond(
                        status = HttpStatusCode.Unauthorized, message = createErrorResponse(
                            code = HttpStatusCode.Unauthorized.value,
                            message = "Unauthorized access. User ID mismatch.",
                            details = "The user ID in the request does not match the authenticated user ID."
                        )
                    )
                    return@checkDeviceIntegrity
                }
                try {
                    val isPasswordCorrect = userLogInRepository.checkUserPassword(
                        userId = setUpUserProfile.userId,
                        passwordHash = setUpUserProfile.passwordHash
                    )
                    if (isPasswordCorrect) {
                        call.respond(
                            status = HttpStatusCode.OK, message = true
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.Unauthorized, message = createErrorResponse(
                                code = HttpStatusCode.Unauthorized.value,
                                message = "Incorrect password.",
                                details = "The provided password is incorrect. Please try again."
                            )
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Failed to check user password.",
                            details = e.message ?: "Unknown error occurred while checking user password."
                        )
                    )
                }
            }
        }
    }
}

fun Routing.logInWithOAuth() {
    authenticate("auth-oauth-google") {
        get(LoginRoutes.LogInWithOAuth.path) {}
        get(LoginRoutes.OAuthLoginCallback.path) {
            val tokenResponse = call.principal<OAuthAccessTokenResponse.OAuth2>()
            if (tokenResponse == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized, message = createErrorResponse(
                        code = HttpStatusCode.Unauthorized.value,
                        message = "Unauthorized access. Please provide a valid OAuth token.",
                        details = """
                            The request was denied due to missing or invalid OAuth credentials.
                            Please include a valid OAuth token in the request header.
                            If you do not have a token, please contact the system administrator to obtain one.
                        """.trimIndent()
                    )
                )
                return@get
            }
            try {
                val response = fetchUserInfo(tokenResponse.accessToken)
                val authTokenService: UserLogInRepository = DaggerMySqlComponent.create().getUserRepository()
                val loggedUser = authTokenService.getUserByEmail(response.toUserEntity().email)
                if (loggedUser == null) {
                    call.respond(
                        status = HttpStatusCode.NotFound, message = createErrorResponse(
                            code = HttpStatusCode.NotFound.value,
                            message = "User not found.",
                            details = "To use this service, please register first from the mobile app."
                        )
                    )
                    return@get
                }
                val isProfileDone = authTokenService.isProfileSetUpDone(loggedUser.uuid)
                call.respond(
                    status = HttpStatusCode.OK, message = UserLogInResponse(
                        user = loggedUser, isProfileSetUpDone = isProfileDone
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = createErrorResponse(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Failed to fetch user information.",
                        details = e.message ?: "Unknown error occurred while fetching user info."
                    )
                )
            }
        }
    }
}


fun Routing.checkUserNameAvailable() {
    authenticate("auth-bearer") {
        get(LoginRoutes.CheckUserNameAvailability.path) {
            val authTokenService: UserLogInRepository = DaggerMySqlComponent.create().getUserRepository()
            checkDeviceIntegrity(
                userLogInRepository = authTokenService,
            ) {
                val userName = call.queryParameters["username"] ?: ""
                if (userName.isBlank()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Username cannot be empty.",
                            details = "Please provide a valid username to check availability."
                        )
                    )
                    return@checkDeviceIntegrity
                }

                try {
                    val isAvailable = authTokenService.checkIsUserNameAvailable(userName)
                    if (!isAvailable) {
                        call.respond(
                            status = HttpStatusCode.Conflict, message = createErrorResponse(
                                code = HttpStatusCode.Conflict.value,
                                message = "Username is already taken.",
                                details = "Please choose a different username."
                            )
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK, message = true
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = createErrorResponse(
                            code = HttpStatusCode.BadRequest.value,
                            message = "Failed to check username availability.",
                            details = e.message ?: "Unknown error occurred while checking username availability."
                        )
                    )
                }
            }
        }
    }
}

//fun Routing.checkAuth() {
//    get("/test-google-auth") {
//        val accessToken = call.request.queryParameters["token"]
//        if (accessToken != null) {
//            try {
//                val userInfo = fetchUserInfo(accessToken)
//                call.respond(userInfo)
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
//            }
//        } else {
//            call.respond(HttpStatusCode.BadRequest, "Please provide token parameter")
//        }
//    }
//}