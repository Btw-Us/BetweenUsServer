/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 10:10 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.auth_token

import com.aatech.database.mysql.model.AuthToken

interface AuthTokenRepository {
    suspend fun createAuthToken(
        model: AuthToken
    ): AuthToken

    suspend fun getAuthTokenById(id: Int): AuthToken?

    suspend fun getAuthTokenByToken(token: String): AuthToken?

    suspend fun deleteAuthTokenByToken(token: String): Boolean

    suspend fun deleteAllAuthTokensByUserId(): Boolean

    suspend fun isTokenValid(token: String): Boolean

    suspend fun getAllAuthTokens(): List<AuthToken>

}