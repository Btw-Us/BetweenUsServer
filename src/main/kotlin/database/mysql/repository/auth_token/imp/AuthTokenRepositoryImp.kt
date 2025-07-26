/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 10:12 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.auth_token.imp

import com.aatech.data.mysql.model.AuthToken
import com.aatech.data.mysql.model.AuthTokenTable
import com.aatech.data.mysql.model.GeneratedFrom
import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class AuthTokenRepositoryImp : AuthTokenRepository {
    override suspend fun createAuthToken(model: AuthToken): AuthToken = transaction {
        val insetAuthToken = AuthTokenTable.insert {
            it[generatedFrom] = model.generatedFrom.name
            it[token] = model.token
            it[userId] = model.userId
            it[expiresAt] = model.expiresAt
            it[createdAt] = model.createdAt
        }
        if (insetAuthToken.insertedCount > 0) {
            model.copy(id = insetAuthToken[AuthTokenTable.id].toLong())
        } else {
            throw Exception("Failed to create AuthToken")
        }
    }

    override suspend fun getAuthTokenById(id: Int): AuthToken? {
        return transaction {
            AuthTokenTable.selectAll()
                .where { AuthTokenTable.id eq id }
                .mapNotNull { rowToUser(it) }
                .singleOrNull()
        }
    }

    override suspend fun getAuthTokenByToken(token: String): AuthToken? {
        return transaction {
            AuthTokenTable.selectAll()
                .where { AuthTokenTable.token eq token }
                .mapNotNull { rowToUser(it) }
                .singleOrNull()
        }
    }

    override suspend fun deleteAuthTokenByToken(token: String): Boolean {
        return transaction {
            val deletedCount = AuthTokenTable.deleteWhere { AuthTokenTable.token eq token }
            deletedCount > 0
        }
    }

    override suspend fun deleteAllAuthTokensByUserId(): Boolean {
        return transaction {
            val deletedCount = AuthTokenTable.deleteWhere { AuthTokenTable.userId.isNotNull() }
            deletedCount > 0
        }
    }

    override suspend fun isTokenValid(token: String): Boolean {
        return transaction {
            AuthTokenTable.selectAll()
                .where { AuthTokenTable.token eq token }
                .mapNotNull { rowToUser(it) }
                .any { it.expiresAt == null || it.expiresAt > System.currentTimeMillis() }
        }
    }

    override suspend fun getAllAuthTokens(): List<AuthToken> {
        return transaction {
            AuthTokenTable.selectAll()
                .map { rowToUser(it) }
        }
    }

    fun rowToUser(row: ResultRow): AuthToken {
        return AuthToken(
            id = row[AuthTokenTable.id].toLong(),
            generatedFrom = row[AuthTokenTable.generatedFrom].let { GeneratedFrom.valueOf(it) },
            token = row[AuthTokenTable.token],
            userId = row[AuthTokenTable.userId],
            expiresAt = row[AuthTokenTable.expiresAt],
            createdAt = row[AuthTokenTable.createdAt]
        )
    }
}