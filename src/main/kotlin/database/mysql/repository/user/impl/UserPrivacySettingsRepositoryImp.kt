/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:24 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.user.impl

import com.aatech.database.mysql.mapper.rowToUserPrivacySettings
import com.aatech.database.mysql.model.UserPrivacySettingsTable
import com.aatech.database.mysql.model.entity.UserPrivacySettings
import com.aatech.database.mysql.repository.user.UserPrivacySettingsRepository
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class UserPrivacySettingsRepositoryImp : UserPrivacySettingsRepository {
    override suspend fun createUser(userPrivacySettings: UserPrivacySettings): UserPrivacySettings {
        val query = UserPrivacySettingsTable.insert {
            it[userId] = userPrivacySettings.userId
            it[allowProfilePicture] = userPrivacySettings.allowProfilePicture.name
            it[allowLastSeen] = userPrivacySettings.allowLastSeen.name
            it[allowReadReceipts] = userPrivacySettings.allowReadReceipts
        }
        if (query.insertedCount > 0) {
            return userPrivacySettings.copy(userId = query[UserPrivacySettingsTable.userId])
        } else {
            throw Exception("Failed to create User Privacy Settings")
        }
    }

    override suspend fun getUserById(userId: String): UserPrivacySettings? {
        return UserPrivacySettingsTable.selectAll()
            .where { UserPrivacySettingsTable.userId eq userId }
            .mapNotNull(::rowToUserPrivacySettings).singleOrNull()
    }
}
