/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 8:34 PM
 * Created by: ayaan
 *
 */

package com.aatech.config.api_config

import com.aatech.config.response.createErrorResponse
import com.aatech.dagger.components.DaggerMySqlComponent
import com.aatech.database.mysql.repository.user.UserLogInRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun RoutingContext.checkAuth(
    onSuccess: suspend (AuthenticationParams) -> Unit
) {
    try {
        val tokenPrincipal = call.principal<BearerTokenCredential>()
        if (tokenPrincipal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                createErrorResponse(
                    message = "Unauthorized",
                    code = HttpStatusCode.Unauthorized.value,
                    details = "No valid authentication token provided."
                )
            )
            return
        }

        val authTokenService = DaggerMySqlComponent.create().getAuthTokenService()
        // Validate the token
        val isValidToken = authTokenService.isTokenValid(tokenPrincipal.token)
        if (!isValidToken) {
            call.respond(
                HttpStatusCode.Unauthorized,
                createErrorResponse(
                    message = "Unauthorized",
                    code = HttpStatusCode.Unauthorized.value,
                    details = "Invalid or expired authentication token."
                )
            )
            return
        }

        // Now we can access call and validate headers
        val clientType = call.request.header("X-Client-Type")?.let {
            try {
                ClientType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                ClientType.OTHER
            }
        } ?: ClientType.OTHER

        // Create authentication parameters with full validation
        val authParam = AuthenticationParams(
            clientType = clientType,
            authToken = tokenPrincipal.token,
            userId = call.request.header("X-User-Id"),
            clientVersion = call.request.header("X-Client-Version"),
            deviceId = call.request.header("X-Device-Id"),
            session = call.request.header("X-Session"),
            deviceModel = call.request.header("X-Device-Model")
        )

        // Validate authentication parameters
        val validationError = authParam.isAuthenticationParams()
        if (validationError != null) {
            call.respond(
                status = if (validationError.errorCode == HttpStatusCode.Unauthorized.value) {
                    HttpStatusCode.Unauthorized
                } else {
                    HttpStatusCode.BadRequest
                },
                createErrorResponse(
                    message = validationError.errorMessage,
                    code = validationError.errorCode,
                    details = validationError.details
                )
            )
            return
        }

        // Your health check logic here - authentication successful
        onSuccess(authParam)
    } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            createErrorResponse(
                message = "Internal server error",
                code = HttpStatusCode.InternalServerError.value,
                details = e.message ?: "An unexpected error occurred"
            )
        )
    }
}

/**
 * Checks the device integrity of the user.
 * This function validates the authentication parameters and checks if the device is authorized for the user.
 * @param isCheckForUserId If true, checks if the user ID is provided.
 * @param userLogInRepository Optional UserRepository to check if the device is valid for the user.
 * @param onSuccess Callback function to be invoked if the device integrity check passes.
 *
 */
suspend fun RoutingContext.checkDeviceIntegrity(
    isCheckForUserId: Boolean = true,
    userLogInRepository: UserLogInRepository? = null,
    onSuccess: suspend (AuthenticationParams) -> Unit
) {
    checkAuth { authParam ->
        if (isCheckForUserId && (authParam.userId == null || authParam.userId.isEmpty())) {
            call.respond(
                status = HttpStatusCode.BadRequest, message = createErrorResponse(
                    code = HttpStatusCode.BadRequest.value,
                    message = "User ID is required.",
                    details = "Please provide a valid user ID in the request."
                )
            )
            return@checkAuth
        }
        if (authParam.clientVersion == null || authParam.clientVersion.isEmpty()) {
            call.respond(
                status = HttpStatusCode.BadRequest, message = createErrorResponse(
                    code = HttpStatusCode.BadRequest.value,
                    message = "Client version is required.",
                    details = "Please provide a valid client version in the request."
                )
            )
            return@checkAuth
        }
        if (authParam.deviceId == null || authParam.deviceId.isEmpty()) {
            call.respond(
                status = HttpStatusCode.BadRequest, message = createErrorResponse(
                    code = HttpStatusCode.BadRequest.value,
                    message = "Device ID is required.",
                    details = "Please provide a valid device ID in the request."
                )
            )
            return@checkAuth
        }
        if (userLogInRepository != null) {
            val isDeviceValid = userLogInRepository.checkIsUserDeviceValid(
                userId = authParam.userId ?: "",
                deviceId = authParam.deviceId
            )
            if (!isDeviceValid) {
                call.respond(
                    status = HttpStatusCode.Unauthorized, message = createErrorResponse(
                        code = HttpStatusCode.Unauthorized.value,
                        message = "Unauthorized device",
                        details = "The device is not authorized for the user."
                    )
                )
                return@checkAuth
            }
        }
        onSuccess.invoke(
            authParam
        )
    }
}