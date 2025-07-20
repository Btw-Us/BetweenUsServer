/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 8:45 PM
 * Created by: ayaan
 *
 */

package com.aatech.plugin

import com.aatech.data.mysql.config.DatabaseConfig
import com.aatech.data.mysql.model.AuthTokenTable
import com.aatech.data.mysql.model.UserPrivacySettingsTable
import com.aatech.data.mysql.model.UserStatusTable
import com.aatech.data.mysql.model.UserTable
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabases() {
    val database = DatabaseConfig.init()
    transaction(database) {
        SchemaUtils.create(
            AuthTokenTable,
            UserTable,
            UserPrivacySettingsTable,
            UserStatusTable
        )
    }
}