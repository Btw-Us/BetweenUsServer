/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:14 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.user.impl

import com.aatech.database.mysql.mapper.rowToUser
import com.aatech.database.mysql.mapper.rowToUserPassword
import com.aatech.database.mysql.model.UserDevicesTable
import com.aatech.database.mysql.model.UserPasswordTable
import com.aatech.database.mysql.model.UserPrivacySettingsTable
import com.aatech.database.mysql.model.UserTable
import com.aatech.database.mysql.model.entity.SetUpUserProfile
import com.aatech.database.mysql.model.entity.User
import com.aatech.database.mysql.model.entity.UserLogInResponse
import com.aatech.database.mysql.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class UserRepositoryImp : UserRepository {
    override suspend fun createUser(
        user: User, deviceInfo: Pair<String, String>
    ): UserLogInResponse = withContext(Dispatchers.IO) {
        transaction {
            val existingUser =
                UserTable.selectAll().where { UserTable.uuid eq user.uuid }.mapNotNull(::rowToUser).singleOrNull()

            if (existingUser != null) {
                val newLastLogin = System.currentTimeMillis()
                UserTable.update({ UserTable.uuid eq user.uuid }) {
                    it[lastLogin] = newLastLogin
                }

                val updatedUser = existingUser.copy(lastLogin = newLastLogin)

                return@transaction UserLogInResponse(
                    updatedUser, isNewUser = false
                )
            }
            val insertUser = UserTable.insert {
                it[uuid] = user.uuid
                it[username] = user.username
                it[fullName] = user.fullName
                it[email] = user.email
                it[profilePicture] = user.profilePicture
                it[createdAt] = user.createdAt
                it[updatedAt] = user.updatedAt
                it[lastLogin] = user.lastLogin
                it[isProfileActive] = user.isProfileActive
            }
            if (insertUser.insertedCount > 0) {
                val userPrivacySettings = UserPrivacySettingsTable.insert {
                    it[userId] = user.uuid
                }
                if (userPrivacySettings.insertedCount <= 0) {
                    throw Exception("Failed to create User Privacy Settings")
                }
                val insertUserDevice = UserDevicesTable.insert {
                    it[userId] = user.uuid
                    it[deviceId] = deviceInfo.first
                    it[deviceName] = deviceInfo.second
                }
                if (insertUserDevice.insertedCount <= 0) {
                    throw Exception("Failed to create User Device")
                }
                val currentUser =
                    UserTable.selectAll().where { UserTable.uuid eq user.uuid }.mapNotNull(::rowToUser).singleOrNull()
                UserLogInResponse(
                    currentUser ?: throw Exception("User not found after creation"), isNewUser = true
                )
            } else {
                throw Exception("Failed to create User")
            }

        }
    }

    override suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll().where { UserTable.email eq email }.mapNotNull(::rowToUser).singleOrNull()
        }
    }

    override suspend fun isProfileSetUpDone(userId: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            UserPasswordTable.selectAll().where { UserTable.uuid eq userId }.mapNotNull(::rowToUserPassword)
                .singleOrNull()?.passwordHash != null
        }
    }

    override suspend fun setUpProfile(
        setUpUserProfile: SetUpUserProfile
    ): UserLogInResponse = withContext(Dispatchers.IO) {
        transaction {
            val user = UserTable.selectAll().where { UserTable.uuid eq setUpUserProfile.userId }.mapNotNull(::rowToUser)
                .singleOrNull() ?: throw Exception("User not found")

            UserPasswordTable.insert {
                it[this.userId] = setUpUserProfile.userId
                it[this.passwordHash] = setUpUserProfile.passwordHash
                it[lastPasswordChange] = System.currentTimeMillis()
            }

            UserTable.update({ UserTable.uuid eq setUpUserProfile.userId }) {
                it[username] = setUpUserProfile.userName
            }

            val updatedUser = user.copy(username = setUpUserProfile.userName)
            UserLogInResponse(
                updatedUser, isNewUser = false
            )
        }
    }

    override suspend fun checkUserPassword(userId: String, passwordHash: String): Boolean =
        withContext(Dispatchers.IO) {
            transaction {
                UserPasswordTable.selectAll().where { UserPasswordTable.userId eq userId }
                    .mapNotNull(::rowToUserPassword).singleOrNull()?.passwordHash == passwordHash
            }
        }


}

