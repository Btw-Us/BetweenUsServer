/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 10:26 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.services

import com.aatech.data.mysql.model.AuthToken
import com.aatech.data.mysql.model.GeneratedFrom
import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import javax.inject.Inject

class AuthTokenService @Inject constructor(
    private val authTokenRepository: AuthTokenRepository
) {
    suspend fun createAuthToken(
        model: AuthToken = AuthToken(
            generatedFrom = GeneratedFrom.SERVER,
        )
    ) = authTokenRepository.createAuthToken(model)

    suspend fun getAuthTokenById(id: Int) = authTokenRepository.getAuthTokenById(id)

    suspend fun getAuthTokenByToken(token: String) = authTokenRepository.getAuthTokenByToken(token)

    suspend fun deleteAuthTokenByToken(token: String) = authTokenRepository.deleteAuthTokenByToken(token)

    suspend fun deleteAllAuthTokensByUserId() = authTokenRepository.deleteAllAuthTokensByUserId()

    suspend fun isTokenValid(token: String) = authTokenRepository.isTokenValid(token)

    suspend fun getAllAuthTokens() = authTokenRepository.getAllAuthTokens()
}