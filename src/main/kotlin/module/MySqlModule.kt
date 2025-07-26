/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/26/25, 12:49 PM
 * Created by: ayaan
 *
 */

package com.aatech.module

import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import com.aatech.database.mysql.repository.auth_token.imp.AuthTokenRepositoryImp
import com.aatech.database.mysql.services.AuthTokenService
import com.aatech.plugin.configureMySqlDatabases
import com.aatech.plugin.databaseConfiguration
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.mySqlModule() {
    configureMySqlDatabases()
    dependencies {
        provide<Database> { configureMySqlDatabases() }
        provide<AuthTokenRepository> { AuthTokenRepositoryImp() }
        provide<AuthTokenService> {
            AuthTokenService(
                authTokenRepository = AuthTokenRepositoryImp()
            )
        }
    }
    val database: Database by dependencies
    databaseConfiguration(
        database
    )
}