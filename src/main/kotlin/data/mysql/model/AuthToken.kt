/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 9:14 PM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table

@Serializable
class AuthToken(
    val id: Int,
    val generatedFrom : String,
    val token: String,
    val userId: String?,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)


object AuthTokenTable : Table("auth_tokens"){
    val id = integer("id").autoIncrement()
    val generatedFrom = varchar("generated_from", 255)
    val token = varchar("token", 255).uniqueIndex()
    val userId = varchar("user_id", 255).nullable()
    val expiresAt = long("expires_at").nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}