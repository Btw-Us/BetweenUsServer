/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: September 14, 2025 09:26 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.mongodb.migration

import com.aatech.database.mongodb.model.PersonalChatRoom
import com.aatech.utils.MongoDbCollectionNames
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MigrationEntry(
    @param:BsonId
    val id: String = ObjectId().toString(),
    val migrationId: String,
    val version: Int,
    val description: String,
    val appliedAt: Long = System.currentTimeMillis(),
    val executionTimeMs: Long,
    val status: MigrationStatus = MigrationStatus.EXECUTED,
    val rollbackScript: String? = null
)

enum class MigrationStatus {
    PENDING,
    EXECUTING,
    EXECUTED,
    FAILED,
    ROLLED_BACK
}


// Migration Interface
abstract class Migration {
    abstract val id: String
    abstract val version: Int
    abstract val description: String

    abstract suspend fun up(database: MongoDatabase): MigrationResult
    abstract suspend fun down(database: MongoDatabase): MigrationResult
}

data class MigrationResult(
    val success: Boolean,
    val message: String,
    val documentsAffected: Long = 0,
    val executionTimeMs: Long
)

class MigrationRunner(
    private val appDatabase: MongoDatabase,
    private val migrationDatabase: MongoDatabase
) {
    private val migrationCollection = migrationDatabase.getCollection<MigrationEntry>("migration_history")

    suspend fun runMigrations() {
        val appliedMigrations = getAppliedMigrations()
        val pendingMigrations = getAllMigrations().filter { migration ->
            !appliedMigrations.contains(migration.id)
        }.sortedBy { it.version }

        pendingMigrations.forEach { migration ->
            executeMigration(migration)
        }
    }

    private suspend fun executeMigration(migration: Migration) {
        println("Executing migration: ${migration.id} - ${migration.description}")

        val startTime = System.currentTimeMillis()

        // Record migration as executing
        val migrationEntry = MigrationEntry(
            migrationId = migration.id,
            version = migration.version,
            description = migration.description,
            appliedAt = startTime,
            executionTimeMs = 0,
            status = MigrationStatus.EXECUTING
        )

        try {
            migrationCollection.insertOne(migrationEntry)

            val result = migration.up(appDatabase)
            val executionTime = System.currentTimeMillis() - startTime

            if (result.success) {
                // Update migration as completed
                migrationCollection.updateOne(
                    Filters.eq("migrationId", migration.id),
                    Updates.combine(
                        Updates.set("status", MigrationStatus.EXECUTED),
                        Updates.set("executionTimeMs", executionTime)
                    )
                )
                println("Migration ${migration.id} completed successfully in ${executionTime}ms")
            } else {
                // Mark as failed
                migrationCollection.updateOne(
                    Filters.eq("migrationId", migration.id),
                    Updates.set("status", MigrationStatus.FAILED)
                )
                throw Exception("Migration failed: ${result.message}")
            }

        } catch (e: Exception) {
            migrationCollection.updateOne(
                Filters.eq("migrationId", migration.id),
                Updates.set("status", MigrationStatus.FAILED)
            )
            throw e
        }
    }

    private suspend fun getAppliedMigrations(): List<String> {
        return migrationCollection
            .find(Filters.eq("status", MigrationStatus.EXECUTED))
            .toList()
            .map { it.migrationId }
    }

    private fun getAllMigrations(): List<Migration> {
        return listOf(
            Migration001AddLastMessageId()
        )
    }
}


class Migration001AddLastMessageId : Migration() {
    override val id = "add_last_message_id_to_personal_chat_room"
    override val version = 1
    override val description = "Add lastMessageId field to PersonalChatRoom collection"

    override suspend fun up(database: MongoDatabase): MigrationResult {
        val startTime = System.currentTimeMillis()
        val collection = database.getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)

        try {
            // Add schema version and lastMessageId to documents that don't have it
            val filter = Filters.not(Filters.exists("lastMessageId"))

            val updateResult = collection.updateMany(
                filter,
                Updates.combine(
                    Updates.set("lastMessageId", null),
                    Updates.set("schemaVersion", 2)
                )
            )

            val executionTime = System.currentTimeMillis() - startTime

            return MigrationResult(
                success = true,
                message = "Successfully added lastMessageId field to ${updateResult.modifiedCount} documents",
                documentsAffected = updateResult.modifiedCount,
                executionTimeMs = executionTime
            )

        } catch (e: Exception) {
            return MigrationResult(
                success = false,
                message = "Failed to add lastMessageId field: ${e.message}",
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }

    override suspend fun down(database: MongoDatabase): MigrationResult {
        val startTime = System.currentTimeMillis()
        val collection = database.getCollection<PersonalChatRoom>(MongoDbCollectionNames.PersonalChatRoom.cName)

        try {
            // Remove lastMessageId field and reset schema version
            val updateResult = collection.updateMany(
                Filters.eq("schemaVersion", 2),
                Updates.combine(
                    Updates.unset("lastMessageId"),
                    Updates.set("schemaVersion", 1)
                )
            )

            val executionTime = System.currentTimeMillis() - startTime

            return MigrationResult(
                success = true,
                message = "Successfully removed lastMessageId field from ${updateResult.modifiedCount} documents",
                documentsAffected = updateResult.modifiedCount,
                executionTimeMs = executionTime
            )

        } catch (e: Exception) {
            return MigrationResult(
                success = false,
                message = "Failed to remove lastMessageId field: ${e.message}",
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
}
