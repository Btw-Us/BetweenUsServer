/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 10:27 PM
 * Created by: ayaan
 *
 */

package com.aatech.routes

import com.aatech.config.api_config.AuthRoutes
import com.aatech.data.mysql.services.AuthTokenService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.authToken() {
    post(AuthRoutes.GenerateToken.path) {
        val authTokenService = AuthTokenService()
        val authToken = authTokenService.createAuthToken()
        call.respond(
            HttpStatusCode.Created,
            message = authToken
        )
    }

}