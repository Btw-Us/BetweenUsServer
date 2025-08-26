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

import com.aatech.config.response.AllFriendsResponse
import com.aatech.database.mysql.model.*
import com.aatech.database.mysql.model.entity.*
import org.jetbrains.exposed.v1.core.ResultRow

fun rowToUser(row: ResultRow): User =
    User(
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


fun rowToAlFriendResponse(row: ResultRow): AllFriendsResponse =
    AllFriendsResponse(
        user = User(
            uuid = row[UserTable.uuid],
            username = row[UserTable.username],
            fullName = row[UserTable.fullName] ?: "No Name",
            email = row[UserTable.email],
            profilePicture = row[UserTable.profilePicture],
            createdAt = row[UserTable.createdAt],
            updatedAt = row[UserTable.updatedAt],
            lastLogin = row[UserTable.lastLogin],
            isProfileActive = row[UserTable.isProfileActive],
        ),
        chatRoomPath = row[UserFriendsTable.chatRoomPath]
    )


fun rowToUserPassword(row: ResultRow): UserPassword =
    UserPassword(
        userId = row[UserPasswordTable.userId],
        passwordHash = row[UserPasswordTable.passwordHash],
        lastPasswordChange = row[UserPasswordTable.lastPasswordChange]
    )


fun rowToFriendRequest(row: ResultRow): FriendRequest =
    FriendRequest(
        id = row[FriendsRequestTable.id],
        requesterId = row[FriendsRequestTable.requesterId],
        receiverId = row[FriendsRequestTable.receiverId],
        status = FriendshipRequestStatus.valueOf(row[FriendsRequestTable.status]),
        requestedAt = row[FriendsRequestTable.requestedAt],
        respondedAt = row[FriendsRequestTable.respondedAt]
    )


fun rowToUserPrivacySettings(row: ResultRow): UserPrivacySettings = UserPrivacySettings(
    userId = row[UserPrivacySettingsTable.userId],
    allowProfilePicture = PrivacyVisibility.valueOf(row[UserPrivacySettingsTable.allowProfilePicture]),
    allowLastSeen = PrivacyVisibility.valueOf(row[UserPrivacySettingsTable.allowLastSeen]),
    allowReadReceipts = row[UserPrivacySettingsTable.allowReadReceipts]
)


fun rowToUserStatus(row: ResultRow): UserStatus =
    UserStatus(
        userId = row[UserStatusTable.userId],
        status = row[UserStatusTable.status],
        lastUpdated = row[UserStatusTable.lastUpdated]
    )


fun rowToUserFriends(row: ResultRow): UserFriends =
    UserFriends(
        userId = row[UserFriendsTable.userId],
        friendId = row[UserFriendsTable.friendId],
        createdAt = row[UserFriendsTable.createdAt],
        chatRoomPath = row[UserFriendsTable.chatRoomPath],
        friendshipRequestStatus = FriendshipRequestStatus.valueOf(row[UserFriendsTable.friendshipRequestStatus]),
        isMuted = row[UserFriendsTable.isMuted],
        isFavorite = row[UserFriendsTable.isFavorite],
        lastMessage = row[UserFriendsTable.lastMessage],
        lastMessageAt = row[UserFriendsTable.lastMessageAt],
        id = row[UserFriendsTable.id]
    )


fun rowToUserNotificationToken(row: ResultRow): UserNotificationToken =
    UserNotificationToken(
        userId = row[UserNotificationTokenTable.userId],
        token = row[UserNotificationTokenTable.token],
        createdAt = row[UserNotificationTokenTable.createdAt],
        updatedAt = row[UserNotificationTokenTable.updatedAt]
    )

