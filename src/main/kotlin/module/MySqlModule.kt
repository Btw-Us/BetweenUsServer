/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/26/25, 12:49 PM
 * Created by: ayaan
 *
 */

package com.aatech.module

import com.aatech.plugin.configureMySqlDatabases
import io.ktor.server.application.*

fun Application.mySqlModule() {
    configureMySqlDatabases()
}