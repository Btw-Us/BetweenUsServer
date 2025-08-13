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
            FriendsTable,
            UserPasswordTable
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

    val encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
    val encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString())

    val temp= "mongodb://root:Admin%40123@mongo1:27017,mongo2:27017,mongo3:27017/between_us_db?replicaSet=myReplicaSet&authSource=admin"
    val connectionString = "mongodb://$encodedUserName:$encodedPassword@$mangoDbUrl/$databaseName?replicaSet=myReplicaSet&authSource=admin"
    val mongoClient = MongoClient.create(connectionString)
    val database = mongoClient.getDatabase(databaseName)

    runBlocking(Dispatchers.IO) {
        database.createCollection(
            MongoDbCollectionNames.PersonalChatRoom.cName,
        )
        database.createCollection(
            MongoDbCollectionNames.Message.cName,
        )

        val personalChatRoomCollection = database
            .getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)
        val messagesCollection = database
            .getCollection<Message>(MongoDbCollectionNames.Message.cName)
        createIndexes(personalChatRoomCollection, messagesCollection)
    }
    return database
}

suspend fun createIndexes(
    chatRoomsCollection: MongoCollection<PersonalChatRoom>,
    messagesCollection: MongoCollection<Message>
) {
    // ChatRoom indexes
    chatRoomsCollection.createIndex(Indexes.ascending("userId"))
    chatRoomsCollection.createIndex(Indexes.ascending("friendId"))
    chatRoomsCollection.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("userId"),
            Indexes.ascending("friendId")
        )
    )
    chatRoomsCollection.createIndex(Indexes.ascending("userName"))
    chatRoomsCollection.createIndex(Indexes.ascending("friendName"))
    chatRoomsCollection.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("userName"),
            Indexes.ascending("friendName")
        )
    )


    // Message indexes
    messagesCollection.createIndex(Indexes.ascending("chatRoomId"))
    messagesCollection.createIndex(Indexes.ascending("userId"))
    messagesCollection.createIndex(Indexes.ascending("friendId"))
    messagesCollection.createIndex(Indexes.ascending("timestamp"))
    messagesCollection.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("chatRoomId"),
            Indexes.ascending("timestamp")
        )
    )
}