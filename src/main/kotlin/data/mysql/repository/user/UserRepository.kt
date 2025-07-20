/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:11 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.repository.user

import com.aatech.data.mysql.model.User


interface UserRepository{
    suspend fun createUser(
        user : User
    ) : User

    suspend fun getUserById(
        userId: Long
    ): User?
}