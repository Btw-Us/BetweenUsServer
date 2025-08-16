package com.aatech.database.mysql.model.entity

import com.aatech.database.mysql.model.ActiveStatus
import com.aatech.database.mysql.model.FriendshipRequestStatus
import com.aatech.database.mysql.model.PrivacyVisibility
import com.aatech.utils.generateUuidFromSub
import kotlinx.serialization.Serializable
import kotlin.random.Random


@Serializable
data class RegisterUserRequest(
    val email: String,
    val fullName: String,
    val profilePicture: String? = null,
)

fun RegisterUserRequest.toUserEntity(): User {
    return User(
        uuid = email.generateUuidFromSub().toString(),
        username = "${fullName.replace(" ", "").lowercase()}@${Random(System.currentTimeMillis()).nextInt()}",
        email = email.lowercase(),
        fullName = fullName,
        profilePicture = profilePicture
    )
}

@Serializable
data class UserLogInResponse(
    val user: User, val isProfileSetUpDone: Boolean = false
)

@Serializable
data class User(
    val uuid: String,
    val username: String,
    val email: String,
    val fullName: String,
    val profilePicture: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val isProfileActive: Boolean = true,
)


@Serializable
data class SetUpUserProfile(
    val userId: String,
    val userName: String,
    val passwordHash: String,
)


@Serializable
data class UserPassword(
    val userId: String,
    val passwordHash: String? = null,
    val lastPasswordChange: Long = System.currentTimeMillis()
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
data class FriendRequest(
    val id: String, // UUID for unique friendship record
    val requesterId: String,
    val receiverId: String,
    val status: FriendshipRequestStatus = FriendshipRequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

@Serializable
data class UserFriends(
    val id: String,
    val userId: String,
    val friendId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val chatRoomPath: String? = null,
    val friendshipRequestStatus: FriendshipRequestStatus = FriendshipRequestStatus.PENDING,
    val isMuted: Boolean = false,
    val isFavorite: Boolean = false,
    val lastMessage: String? = null,
    val lastMessageAt: Long? = null
)

