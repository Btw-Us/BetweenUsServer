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

import com.aatech.plugin.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.mainModule() {
    configureAuthentication()
    configureWebSocket()
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureMySqlDatabases()
    configureMongoDB()
}