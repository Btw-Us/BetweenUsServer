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
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.BearerTokenCredential
import io.ktor.server.auth.principal
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

suspend fun RoutingContext.checkAuth(
    onSuccess:suspend (AuthenticationParams) -> Unit
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
            deviceName = call.request.header("X-Device-Name")
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