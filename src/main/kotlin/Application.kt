/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 6:24 PM
 * Created by: ayaan
 *
 */

package com.aatech

import com.aatech.server_config.configureAuthentication
import com.aatech.config.configureFrameworks
import com.aatech.config.configureMonitoring
import com.aatech.server_config.configureRouting
import com.aatech.config.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureAuthentication()
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureFrameworks()
}
