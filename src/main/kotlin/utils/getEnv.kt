/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/20/25, 9:04 PM
 * Created by: ayaan
 *
 */

package com.aatech.utils

fun getEnv(
    key: String,
    default: String? = null
): String? {
    val dotenv = io.github.cdimascio.dotenv.dotenv()
    return System.getenv(key)
        ?: dotenv[key]
        ?: default
        ?: throw IllegalArgumentException("Environment variable '$key' is not set and no default value provided.")
}