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
import com.aatech.utils.getEnv
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils

fun configureMySqlDatabases() {
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

fun configureMongoDB(): MongoDatabase {
    val mongoClient = MongoClient.create("mongodb://localhost:27017")
    val database = mongoClient.getDatabase(
        getEnv("DATABASE_NAME") ?: throw IllegalArgumentException("MYSQL_DATABASE environment variable is not set")
    )
    return database
}