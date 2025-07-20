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

import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.auth.bearer
import kotlin.text.isEmpty

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
}