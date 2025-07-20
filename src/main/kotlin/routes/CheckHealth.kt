/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: 7/20/25, 5:31 PM
 * Author: ayaan
 * Project: BetweenUsServer
 *
 * No part of this work may be reproduced, distributed, or transmitted in any form
 * or by any means, including photocopying, recording, or other electronic or
 * mechanical methods, without the prior written permission of the copyright holder.
 *
 * For permission requests, contact: ayaan35200@gmail.com
 */

package com.aatech.routes


import com.aatech.config.api_config.AuthenticationParams
import com.aatech.config.api_config.ClientType
import com.aatech.config.api_config.HEALTH
import com.aatech.config.response.createErrorResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.health() {
    authenticate("auth-bearer") {
        get(HEALTH) {
            try {
                val tokenPrincipal = call.principal<BearerTokenCredential>()
                println("Health check request received with token: ${tokenPrincipal}")
                if (tokenPrincipal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        createErrorResponse(
                            message = "Unauthorized",
                            code = HttpStatusCode.Unauthorized.value,
                            details = "No valid authentication token provided."
                        )
                    )
                    return@get
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
                    return@get
                }

                // Your health check logic here - authentication successful
                call.respondText("OK - Service is healthy")
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
    }
}