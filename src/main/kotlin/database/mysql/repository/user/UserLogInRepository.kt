/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 1:11 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.user

import com.aatech.database.mysql.model.entity.OperationType
import com.aatech.database.mysql.model.entity.SetUpUserProfile
import com.aatech.database.mysql.model.entity.User
import com.aatech.database.mysql.model.entity.UserLogInResponse
import com.aatech.database.mysql.model.entity.UserNotificationToken


interface UserLogInRepository {
    /**
     * Creates a new user in the database.
     * If the user already exists, it updates the last login time.
     * @param user The user to be created or updated.
     * @param deviceInfo Pair containing device ID and device name.
     * @return UserLogInResponse containing user details and a flag indicating if it's a new
     */
    suspend fun createUser(
        user: User,
        deviceInfo: Pair<String, String>
    ): UserLogInResponse

    suspend fun getUserByEmail(
        email: String
    ): User?


    suspend fun isProfileSetUpDone(
        userId: String
    ): Boolean


    suspend fun setUpProfile(
        setUpUserProfile: SetUpUserProfile
    ): UserLogInResponse


    suspend fun checkIsUserNameAvailable(
        userName: String
    ): Boolean


    suspend fun checkIsUserDeviceValid(
        userId: String,
        deviceId: String
    ): Boolean

    suspend fun checkUserPassword(
        userId: String,
        passwordHash: String
    ): Boolean

    suspend fun addOrUpdateUserNotificationToken(
        userId: String,
        tokenModel : UserNotificationToken
    ): OperationType
}