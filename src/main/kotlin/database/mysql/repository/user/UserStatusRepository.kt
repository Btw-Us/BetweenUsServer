/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:13 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.repository.user

import com.aatech.database.mysql.model.UserStatus

interface UserStatusRepository {
    suspend fun createUser(
        userStatus: UserStatus
    ) : UserStatus

    suspend fun getUserById(
        userId: String
    ): UserStatus?
}