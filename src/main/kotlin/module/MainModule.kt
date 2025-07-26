/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/26/25, 12:48 PM
 * Created by: ayaan
 *
 */

package com.aatech.module

import com.aatech.plugin.configureAuthentication
import com.aatech.plugin.configureMonitoring
import com.aatech.plugin.configureRouting
import com.aatech.plugin.configureSerialization
import io.ktor.server.application.*

fun Application.mainModule() {
    configureAuthentication()
    configureRouting()
    configureSerialization()
    configureMonitoring()
}