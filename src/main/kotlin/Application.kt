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

import com.aatech.plugin.configureAuthentication
import com.aatech.plugin.configureMongoDB
import com.aatech.plugin.configureMonitoring
import com.aatech.plugin.configureMySqlDatabases
import com.aatech.plugin.configureRouting
import com.aatech.plugin.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.mainModule() {
    configureAuthentication()
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureMySqlDatabases()
    configureMongoDB()
}