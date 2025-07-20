/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: 7/20/25, 6:11 PM
 * Author: ayaan
 * Project: BetweenUsServer
 *
 * No part of this work may be reproduced, distributed, or transmitted in any form
 * or by any means, including photocopying, recording, or other electronic or
 * mechanical methods, without the prior written permission of the copyright holder.
 *
 * For permission requests, contact: ayaan35200@gmail.com
 */

package com.aatech.server_config

import com.aatech.config.response.createErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAuthentication() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "Bearer Authentication"
            authenticate { tokenCredential ->
                try {
                    if (tokenCredential.token.isEmpty() || tokenCredential.token == "null") {
                        return@authenticate null
                    }
                    return@authenticate BearerTokenCredential(
                        token = tokenCredential.token,
                    )
                } catch (e: Exception) {
                    return@authenticate null
                }
            }
        }
    }
    install(StatusPages) {
        status(HttpStatusCode.Unauthorized) {
            call.respond(
                HttpStatusCode.Unauthorized,
                createErrorResponse(
                    code = HttpStatusCode.Unauthorized.value,
                    message = "Unauthorized access. Please provide a valid token.",
                    details = """
                Unauthorized access. Please provide a valid Bearer token.
                
                Ensure that you include the 'Authorization' header with a valid Bearer token in your request.
                Example:
                 Authorization: Bearer <your_token_here>
                If you do not have a token, please contact the system administrator to obtain one.
            """.trimIndent()
                )
            )
        }
    }
}


fun Routing.addAuthenticationRoutes(
    build: Route.() -> Unit
) {
    authenticate("auth-bearer") {
        get("/authenticate") {
            call.respondText("You are authenticated!", status = HttpStatusCode.OK)
        }
    }
}