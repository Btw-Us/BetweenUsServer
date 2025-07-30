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

import com.aatech.database.mysql.config.DatabaseConfig
import com.aatech.database.mysql.model.AuthTokenTable
import com.aatech.database.mysql.model.FriendsTable
import com.aatech.database.mysql.model.UserPrivacySettingsTable
import com.aatech.database.mysql.model.UserStatusTable
import com.aatech.database.mysql.model.UserTable
import com.aatech.utils.getEnv
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun configureMySqlDatabases(): Database {
    val database = DatabaseConfig.init()
    databaseConfiguration(database)
    return database
}

fun databaseConfiguration(database: Database) {
    transaction(database) {
        val migrationStatements = MigrationUtils.statementsRequiredForDatabaseMigration(
            AuthTokenTable,
            UserTable,
            UserPrivacySettingsTable,
            UserStatusTable,
            FriendsTable
        )

        migrationStatements.forEach { statement ->
            exec(statement)
        }
    }
}


fun configureMongoDB(): MongoDatabase {
    val userName = getEnv("MANGO_DB_USER_NAME")
    val password = getEnv("MANGO_DB_PASSWORD")
    val databaseName = getEnv("DATABASE_NAME")
    val mangoDbUrl = getEnv("MANGO_DB_URL", "localhost:27017")

    // URL encode the username and password to handle special characters
    val encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
    val encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString())

    // Try authenticating against the admin database instead of the target database
    val connectionString = "mongodb://$encodedUserName:$encodedPassword@$mangoDbUrl/$databaseName?authSource=admin"
    val mongoClient = MongoClient.create(connectionString)
    return mongoClient.getDatabase(databaseName)
}