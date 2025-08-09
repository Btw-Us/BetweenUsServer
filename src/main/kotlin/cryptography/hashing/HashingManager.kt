/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 07, 2025 10:14 PM
 * Author: ayaan
 * Project: BetweenUsAndroid
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.cryptography.hashing

import org.mindrot.jbcrypt.BCrypt

object HashingManager {

    private const val SALT_ROUNDS = 12


    data class HashedPasswordResult(
        val hashedPassword: String,
        val salt: String
    )

    /**
     * Hash password and return both the hashed password and salt
     */
    fun hashPassword(password: String): HashedPasswordResult {
        val salt = BCrypt.gensalt(SALT_ROUNDS)
        val hashedPassword = BCrypt.hashpw(password, salt)
        return HashedPasswordResult(hashedPassword, salt)
    }

    /**
     * Hash password using a provided salt
     */
    fun hashPasswordWithSalt(password: String, salt: String): String {
        return BCrypt.hashpw(password, salt)
    }

    /**
     * Verify password using the stored hashed password
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }

    /**
     * Verify password using plain password, stored salt, and stored hashed password
     */
    fun verifyPasswordWithSalt(plainPassword: String, salt: String, storedHashedPassword: String): Boolean {
        val hashedInput = BCrypt.hashpw(plainPassword, salt)
        return hashedInput == storedHashedPassword
    }

    /**
     * Generate a new salt
     */
    fun generateSalt(): String {
        return BCrypt.gensalt(SALT_ROUNDS)
    }
}