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
import com.aatech.config.api_config.checkAuth
import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.components.DaggerMySqlComponent
import com.aatech.database.mysql.model.entity.RegisterUserRequest
import com.aatech.database.mysql.model.entity.toUserEntity
import com.aatech.database.mysql.repository.user.UserRepository
import com.aatech.plugin.fetchUserInfo
import com.aatech.plugin.toUserEntity
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Routing.allLogInRoutes() {
    userRoutes()
    logInWithOAuth()
    logInWithGoogle()
    checkAuth()
}

fun Routing.logInWithGoogle() {
    authenticate("auth-bearer") {
        post(LoginRoutes.LogInWithGoogle.path) {
            checkAuth { authParam ->
                val user = call.receive<RegisterUserRequest>()
                val authTokenService: UserRepository = DaggerMySqlComponent.create().getUserRepository()
                try {
                    val loggedUser = authTokenService.createUser(user.toUserEntity())
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = loggedUser
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = createErrorResponse(
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

fun Routing.logInWithOAuth() {
    authenticate("auth-oauth-google") {
        get(LoginRoutes.LogInWithOAuth.path) {
        }
        get(LoginRoutes.OAuthLoginCallback.path) {
            val tokenResponse = call.principal<OAuthAccessTokenResponse.OAuth2>()
            if (tokenResponse == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = createErrorResponse(
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
                val authTokenService: UserRepository = DaggerMySqlComponent.create().getUserRepository()
                val loggedUser = authTokenService.createUser(response.toUserEntity())
                call.respond(
                    status = HttpStatusCode.OK,
                    message = loggedUser
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = createErrorResponse(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Failed to fetch user information.",
                        details = e.message ?: "Unknown error occurred while fetching user info."
                    )
                )
            }
        }
    }
}

fun Routing.checkAuth() {
    get("/test-google-auth") {
        val accessToken = call.request.queryParameters["token"]
        if (accessToken != null) {
            try {
                val userInfo = fetchUserInfo(accessToken)
                call.respond(userInfo)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Please provide token parameter")
        }
    }
}

fun Routing.userRoutes() {
//    post(LoginRoutes.LogInOrRegister.path) {
//        checkAuth { checkAuth ->
//            val loggedUser = call.receive<User>()
////            TODO: Implement login or register logic from here
//        }
//    }
}