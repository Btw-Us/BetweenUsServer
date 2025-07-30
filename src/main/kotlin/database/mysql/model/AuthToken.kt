/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 9:14 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import java.util.*

enum class GeneratedFrom {
    SERVER, CLIENT, ADMIN_PANEL
}

@Serializable
data class AuthToken(
    val generatedFrom: GeneratedFrom,
    val token: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val id: Long = 0,
)


object AuthTokenTable : Table("auth_tokens_table") {
    val id = integer("id").autoIncrement()
    val generatedFrom = varchar("generated_from", 255)
    val token = varchar("token", 255).uniqueIndex()
    val userId = varchar("user_id", 255).nullable()
    val expiresAt = long("expires_at").nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_token", true, token)
        index("idx_user_id", true, userId)
    }
}