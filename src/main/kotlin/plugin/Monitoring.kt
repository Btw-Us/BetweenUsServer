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

package com.aatech.plugin

import io.ktor.client.plugins.logging.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        logger = org.slf4j.LoggerFactory.getLogger("ktor.application")

        // Log request details
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val userAgent = call.request.headers["User-Agent"]
            val duration = call.processingTimeMillis()

            "[$status] $httpMethod $uri - ${duration}ms - User-Agent: $userAgent"
        }

        // Filter what to log
        filter { call ->
            // Log all requests, including errors
            true
        }


    }
}