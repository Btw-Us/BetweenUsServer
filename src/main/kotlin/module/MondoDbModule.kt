/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/26/25, 12:50 PM
 * Created by: ayaan
 *
 */

package com.aatech.module

import com.aatech.plugin.configureMongoDB
import io.ktor.server.application.Application

fun Application.mondoDbModule() {
    configureMongoDB()
}