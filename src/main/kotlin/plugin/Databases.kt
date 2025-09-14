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

import com.aatech.database.mongodb.migration.MigrationRunner
import com.aatech.database.mongodb.model.Message
import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.database.mysql.config.DatabaseConfig
import com.aatech.database.mysql.model.*
import com.aatech.utils.MongoDbCollectionNames
import com.aatech.utils.getEnv
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
            UserDevicesTable,
            UserPrivacySettingsTable,
            UserStatusTable,
            FriendsRequestTable,
            UserPasswordTable,
            UserFriendsTable,
            UserNotificationTokenTable
        )

        migrationStatements.forEach { statement ->
            exec(statement)
        }
    }
}


fun configureMongoDB(): MongoDatabase {
    val mangoDbUrl = getEnv("MANGO_DB_URL")
    val userName = getEnv("MANGO_DB_USER_NAME")
    val password = getEnv("MANGO_DB_PASSWORD")
    val databaseName = getEnv("DATABASE_NAME")
    val migrationDatabaseName = getEnv("MIGRATION_DATABASE_NAME", "${databaseName}_migrations")
    val replicaSet = getEnv("MONGO_DB_REPLICA_SET", "rs0")

    val encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
    val encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString())

    val connectionString =
        "mongodb://$encodedUserName:$encodedPassword@$mangoDbUrl/$databaseName?replicaSet=$replicaSet&authSource=admin"
    val mongoClient = MongoClient.create(connectionString)

    // Get both app database and migration database
    val appDatabase = mongoClient.getDatabase(databaseName)
    val migrationDatabase = mongoClient.getDatabase(migrationDatabaseName)

    runBlocking(Dispatchers.IO) {
        // Create application collections
        appDatabase.createCollection(MongoDbCollectionNames.PersonalChatRoom.cName)
        appDatabase.createCollection(MongoDbCollectionNames.Message.cName)

        // Create migration tracking collection
        migrationDatabase.createCollection(MongoDbCollectionNames.MigrationEntry.cName)

        val personalChatRoomCollection =
            appDatabase.getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)
        val messagesCollection = appDatabase.getCollection<Message>(MongoDbCollectionNames.Message.cName)

        // Create indexes
        createIndexes(personalChatRoomCollection, messagesCollection)

        // Run migrations
        runMigrations(appDatabase, migrationDatabase)
    }

    return appDatabase
}

suspend fun runMigrations(appDatabase: MongoDatabase, migrationDatabase: MongoDatabase) {
    try {
        val migrationRunner = MigrationRunner(appDatabase, migrationDatabase)
        migrationRunner.runMigrations()
        println("All migrations executed successfully")
    } catch (e: Exception) {
        println("Migration failed: ${e.message}")
        throw e
    }
}

suspend fun createIndexes(
    chatRoomsCollection: MongoCollection<PersonalChatRoom>, messagesCollection: MongoCollection<Message>
) {
    // ChatRoom indexes
    chatRoomsCollection.createIndex(Indexes.ascending("userId"))
    chatRoomsCollection.createIndex(Indexes.ascending("friendId"))
    chatRoomsCollection.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("userId"), Indexes.ascending("friendId")
        )
    )
    chatRoomsCollection.createIndex(Indexes.ascending("userName"))
    chatRoomsCollection.createIndex(Indexes.ascending("friendName"))
    chatRoomsCollection.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("userName"), Indexes.ascending("friendName")
        )
    )


    // Message indexes
    messagesCollection.createIndex(Indexes.ascending("chatRoomId"))
    messagesCollection.createIndex(Indexes.ascending("userId"))
    messagesCollection.createIndex(Indexes.ascending("friendId"))
    messagesCollection.createIndex(Indexes.ascending("timestamp"))
    messagesCollection.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("chatRoomId"), Indexes.ascending("timestamp")
        )
    )
}