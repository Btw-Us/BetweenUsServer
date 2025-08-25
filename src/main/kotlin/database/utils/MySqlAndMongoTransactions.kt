/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 26, 2025 12:36 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.utils

sealed class TransactionResult<T, R> {
    data class Success<T, R>(val mysqlResult: T, val mongoResult: R) : TransactionResult<T, R>()
    data class Failure<T, R>(
        val exception: Exception,
        val rollbackFailures: List<Exception> = emptyList()
    ) : TransactionResult<T, R>()
}


fun <T, R> mysqlAndMongoTransactions(
    mysqlTransaction: suspend () -> T,
    mongoTransaction: suspend () -> R,
    rollbackMysql: suspend (T) -> Unit,
    rollbackMongo: suspend (R) -> Unit
): ImprovedMySqlAndMongoTransactions<T, R> {
    return ImprovedMySqlAndMongoTransactions(
        mysqlTransaction,
        mongoTransaction,
        rollbackMysql,
        rollbackMongo
    )
}

data class ImprovedMySqlAndMongoTransactions<T, R>(
    private val mysqlTransaction: suspend () -> T,
    private val mongoTransaction: suspend () -> R,
    private val rollbackMysql: suspend (T) -> Unit,
    private val rollbackMongo: suspend (R) -> Unit
) {
    suspend fun execute(): TransactionResult<T, R> {
        var mysqlResult: T? = null
        var mongoResult: R? = null
        var mysqlCompleted = false
        var mongoCompleted = false

        try {
            mysqlResult = mysqlTransaction()
            mysqlCompleted = true

            mongoResult = mongoTransaction()
            mongoCompleted = true

            return TransactionResult.Success(mysqlResult, mongoResult)

        } catch (e: Exception) {
            val rollbackExceptions = mutableListOf<Exception>()

            if (mongoCompleted && mongoResult != null) {
                try {
                    rollbackMongo(mongoResult)
                } catch (rollbackEx: Exception) {
                    rollbackExceptions.add(rollbackEx)
                }
            }

            if (mysqlCompleted && mysqlResult != null) {
                try {
                    rollbackMysql(mysqlResult)
                } catch (rollbackEx: Exception) {
                    rollbackExceptions.add(rollbackEx)
                }
            }

            return TransactionResult.Failure(e, rollbackExceptions)
        }
    }
}