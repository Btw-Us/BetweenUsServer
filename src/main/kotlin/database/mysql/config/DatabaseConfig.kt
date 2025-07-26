/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 8:54 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.config


import com.aatech.utils.getEnv
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.sql.Connection


object DatabaseConfig {

    fun init(): Database =
        Database.connect(createHikariDataSource()).apply {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }


    private fun createHikariDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = getEnv("DATABASE_URL")
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = getEnv("MYSQL_USER_NAME")
            password = getEnv("MYSQL_PASSWORD")
            maximumPoolSize = 10
            minimumIdle = 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        // First, create the database if it doesn't exist
        val dbName = config.jdbcUrl.substringAfterLast("/")
        val baseJdbcUrl = config.jdbcUrl.substringBeforeLast("/")
        createDatabaseIfNotExists(baseJdbcUrl, dbName, config.username, config.password)

        return HikariDataSource(config)
    }

    private fun createDatabaseIfNotExists(
        baseJdbcUrl: String,
        dbName: String,
        username: String,
        password: String
    ) {
        val tempConfig = HikariConfig().apply {
            jdbcUrl = baseJdbcUrl
            driverClassName = "com.mysql.cj.jdbc.Driver"
            this.username = username
            this.password = password
            maximumPoolSize = 1 // Minimal pool for this operation
        }

        HikariDataSource(tempConfig).use { tempDataSource ->
            tempDataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS $dbName")
                }
            }
        }
    }
}