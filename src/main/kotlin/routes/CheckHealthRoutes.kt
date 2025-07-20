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


import com.aatech.config.api_config.HEALTH
import com.aatech.config.api_config.checkAuth
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.checkHealth() {
    authenticate("auth-bearer") {
        get(HEALTH) {
            checkAuth { authParam ->
                call.respond(
                    HttpStatusCode.OK,
                    message = authParam
                )
            }
        }
    }
}

