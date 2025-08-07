/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: July 30, 2025 10:35 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */


package com.aatech.database.mysql.mapper

import com.aatech.database.mysql.model.*
import com.aatech.database.mysql.model.entity.*
import org.jetbrains.exposed.v1.core.ResultRow

fun rowToUser(row: ResultRow): User {
    return User(
        uuid = row[UserTable.uuid],
        username = row[UserTable.username],
        fullName = row[UserTable.fullName] ?: "No Name",
        email = row[UserTable.email],
        profilePicture = row[UserTable.profilePicture],
        createdAt = row[UserTable.createdAt],
        updatedAt = row[UserTable.updatedAt],
        lastLogin = row[UserTable.lastLogin],
        isProfileActive = row[UserTable.isProfileActive],
    )
}

fun rowToUserPassword(row: ResultRow): UserPassword {
    return UserPassword(
        userId = row[UserPasswordTable.userId],
        passwordHash = row[UserPasswordTable.passwordHash],
        lastPasswordChange = row[UserPasswordTable.lastPasswordChange]
    )
}

fun rowToFriend(row: ResultRow): Friend {
    return Friend(
        id = row[FriendsTable.id],
        requesterId = row[FriendsTable.requesterId],
        receiverId = row[FriendsTable.receiverId],
        status = FriendshipStatus.valueOf(row[FriendsTable.status]),
        createdAt = row[FriendsTable.createdAt],
        updatedAt = row[FriendsTable.updatedAt],
        requestedAt = row[FriendsTable.requestedAt],
        respondedAt = row[FriendsTable.respondedAt]
    )
}


fun rowToUserPrivacySettings(row: ResultRow): UserPrivacySettings {
    return UserPrivacySettings(
        userId = row[UserPrivacySettingsTable.userId],
        allowProfilePicture = PrivacyVisibility.valueOf(row[UserPrivacySettingsTable.allowProfilePicture]),
        allowLastSeen = PrivacyVisibility.valueOf(row[UserPrivacySettingsTable.allowLastSeen]),
        allowReadReceipts = row[UserPrivacySettingsTable.allowReadReceipts]
    )
}

fun rowToUserStatus(row: ResultRow): UserStatus {
    return UserStatus(
        userId = row[UserStatusTable.userId],
        status = row[UserStatusTable.status],
        lastUpdated = row[UserStatusTable.lastUpdated]
    )
}


