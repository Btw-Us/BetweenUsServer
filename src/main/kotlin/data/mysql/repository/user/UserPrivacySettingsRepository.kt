/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:12 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.repository.user

import com.aatech.data.mysql.model.UserPrivacySettings

interface UserPrivacySettingsRepository {
    suspend fun createUser(
        userPrivacySettings: UserPrivacySettings
    ) : UserPrivacySettings

    suspend fun getUserById(
        userId: String
    ): UserPrivacySettings?
}