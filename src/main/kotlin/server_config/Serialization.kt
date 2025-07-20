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

package com.aatech.config

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
