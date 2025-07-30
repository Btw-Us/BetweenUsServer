package com.aatech.database.mysql.model.entity

import com.aatech.database.mysql.model.ActiveStatus
import com.aatech.database.mysql.model.FriendshipStatus
import com.aatech.database.mysql.model.PrivacyVisibility
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uuid: String,
    val clientId: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val profilePicture: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val lastLogin: Long? = null,
    val isProfileActive: Boolean = true,
)


@Serializable
data class UserPrivacySettings(
    val userId: String,
    val allowProfilePicture: PrivacyVisibility = PrivacyVisibility.PUBLIC,
    val allowLastSeen: PrivacyVisibility = PrivacyVisibility.PUBLIC,
    val allowReadReceipts: Boolean = true
)


@Serializable
data class UserStatus(
    val userId: String,
    val status: String = ActiveStatus.OFFLINE.name,
    val lastUpdated: Long = System.currentTimeMillis()
)


@Serializable
data class UserDevice(
    val userId: String,
    val deviceId: String,
    val deviceName: String,
    val devicePublicKey: String,
    val encryptedKeyMaterial: String,
    val keyDerivationSalt: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)


@Serializable
data class Friend(
    val id: String, // UUID for unique friendship record
    val requesterId: String,
    val receiverId: String,
    val status: FriendshipStatus = FriendshipStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)
