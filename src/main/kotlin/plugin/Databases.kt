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
import com.aatech.data.mysql.model.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils

fun configureDatabases() {
    val database = DatabaseConfig.init()
    transaction(database) {
        MigrationUtils.statementsRequiredForDatabaseMigration(
            AuthTokenTable,
            UserTable,
            UserPrivacySettingsTable,
            UserStatusTable,
            FriendsTable
        )
    }
}