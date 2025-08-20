/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 12:52 AM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.model

import com.aatech.database.mysql.model.FriendsRequestTable.id
import com.aatech.database.mysql.model.FriendsRequestTable.receiverId
import com.aatech.database.mysql.model.FriendsRequestTable.requestedAt
import com.aatech.database.mysql.model.FriendsRequestTable.requesterId
import com.aatech.database.mysql.model.FriendsRequestTable.respondedAt
import com.aatech.database.mysql.model.FriendsRequestTable.status
import com.aatech.database.mysql.model.UserDevicesTable.deviceId
import com.aatech.database.mysql.model.UserDevicesTable.deviceName
import com.aatech.database.mysql.model.UserDevicesTable.devicePublicKey
import com.aatech.database.mysql.model.UserDevicesTable.encryptedKeyMaterial
import com.aatech.database.mysql.model.UserDevicesTable.keyDerivationSalt
import com.aatech.database.mysql.model.UserDevicesTable.lastUsedAt
import com.aatech.database.mysql.model.UserDevicesTable.userId
import com.aatech.database.mysql.model.UserPasswordTable.lastPasswordChange
import com.aatech.database.mysql.model.UserPasswordTable.passwordHash
import com.aatech.database.mysql.model.UserPasswordTable.userId
import com.aatech.database.mysql.model.UserPrivacySettingsTable.allowLastSeen
import com.aatech.database.mysql.model.UserPrivacySettingsTable.allowProfilePicture
import com.aatech.database.mysql.model.UserPrivacySettingsTable.allowReadReceipts
import com.aatech.database.mysql.model.UserPrivacySettingsTable.userId
import com.aatech.database.mysql.model.UserStatusTable.lastUpdated
import com.aatech.database.mysql.model.UserStatusTable.status
import com.aatech.database.mysql.model.UserStatusTable.userId
import com.aatech.database.mysql.model.UserTable.createdAt
import com.aatech.database.mysql.model.UserTable.email
import com.aatech.database.mysql.model.UserTable.fullName
import com.aatech.database.mysql.model.UserTable.isProfileActive
import com.aatech.database.mysql.model.UserTable.lastLogin
import com.aatech.database.mysql.model.UserTable.profilePicture
import com.aatech.database.mysql.model.UserTable.updatedAt
import com.aatech.database.mysql.model.UserTable.username
import com.aatech.database.mysql.model.UserTable.uuid
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * Represents the user table in the database.
 * This table stores user information such as username, email, profile picture,
 * password hash, and timestamps for creation, updates, and last login.
 * The `uuid` field is the primary key and is auto-incremented.
 * The `username` and `email` fields are unique indexes to ensure no duplicate users.
 * The `profilePicture` field is nullable, allowing users to not have a profile picture.
 * The `passwordHash` field stores the hashed password for security.
 * The `createdAt`, `updatedAt`, and `lastLogin` fields store timestamps for various user activities.
 * The `UserPrivacySettings` table stores user-specific privacy settings,
 * such as whether to allow profile pictures, last seen status, and read receipts.
 * The `UserStatus` table stores the current status of the user (e.g., online, offline)
 * and the last time the status was updated.
 * This structure allows for efficient user management and privacy control within the application.
 *  @property uuid The unique identifier for the user, auto-incremented.
 *  @property username The unique username of the user.
 *  @property email The unique email address of the user.
 *  @property fullName The full name of the user, nullable.
 *  @property profilePicture The URL of the user's profile picture, nullable.
 *  @property createdAt The timestamp when the user was created.
 *  @property updatedAt The timestamp when the user was last updated, nullable.
 *  @property lastLogin The timestamp of the user's last login, nullable.
 *  @property isProfileActive Indicates whether the user's profile is active, defaulting to true.
 *
 *  @see UserPrivacySettingsTable The table storing user-specific privacy settings.
 *  @see UserStatusTable The table storing the current status of the user and the last time it was updated.
 */
object UserTable : Table("user_db") {
    val uuid = varchar("pk_user_id", 255)
    val username = varchar("username", 255).uniqueIndex()
    val fullName = varchar("full_name", 255)
        .nullable().default("Unknown User").check { it.isNotNull() }
    val email = varchar("email", 255).uniqueIndex()
    val profilePicture = varchar("profile_picture", 255).nullable()
    val createdAt = long("created_at").default(System.currentTimeMillis())
    val updatedAt = long("updated_at").default(System.currentTimeMillis())
    val lastLogin = long("last_login").default(System.currentTimeMillis())
    val isProfileActive = bool("is_profile_active").default(true)

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(uuid, name = "pk_user_id")

    init {
        index("idx_username", true, username)
        index("idx_email", true, email)
    }
}


/**
 * Represents the user passwords table in the database.
 * This table stores the password hash for each user, along with the last password change timestamp.
 * The `userId` field is a foreign key referencing the `User` table,
 * ensuring that each password is associated with a valid user.
 * The `passwordHash` field is a string that stores the hashed password,
 * which is nullable and defaults to null.
 * The `lastPasswordChange` field stores the timestamp of the last password change,
 * defaulting to the current time when the record is created.
 * This structure allows for secure storage of user passwords,
 * enabling features such as password updates and security checks.
 *  @property userId The unique identifier for the user, referencing the `User` table.
 *  @property passwordHash The hashed password of the user, nullable and defaults to null.
 *  @property lastPasswordChange The timestamp of the last password change, defaulting to the current time.
 *  @see UserTable The table storing user information, which this table references.
 */
object UserPasswordTable : Table("user_passwords") {
    val userId = varchar("pk_user_id", 255).references(
        UserTable.uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val passwordHash = varchar("password_hash", 255).nullable().default(null)
    val passwordSalt = varchar("password_salt", 64).nullable().default(null)
    val lastPasswordChange = long("last_password_change").default(System.currentTimeMillis())

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(userId, name = "pk_user_passwords")

    init {
        index("idx_user_id", true, userId)
    }
}


/**
 * Represents the user privacy settings table in the database.
 * This table stores user-specific privacy settings,
 * such as whether to allow profile pictures, last seen status, and read receipts.
 * The `userId` field is a foreign key referencing the `User` table,
 * ensuring that each privacy setting is associated with a valid user.
 * The `allowProfilePicture`, `allowLastSeen`, and `allowReadReceipts`
 * fields are boolean values that indicate the user's preferences for these features.
 * This structure allows for efficient management of user privacy settings,
 * enabling users to control their visibility and interactions within the application.
 * @property userId The unique identifier for the user, referencing the `User` table.
 * @property allowProfilePicture Indicates whether the user allows their profile picture to be visible.
 * @property allowLastSeen Indicates whether the user allows others to see their last seen status.
 * @property allowReadReceipts Indicates whether the user allows read receipts for their messages.
 * @see UserTable The table storing user information, which this table references.
 *
 */
object UserPrivacySettingsTable : Table("user_privacy_settings") {
    val userId = varchar("pk_user_id", 255).references(
        uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val allowProfilePicture = varchar("allow_profile_picture", 30).default(PrivacyVisibility.PUBLIC.name)
    val allowLastSeen = varchar("allow_last_seen", 30).default(PrivacyVisibility.PUBLIC.name)
    val allowReadReceipts = bool("allow_read_receipts").default(true)

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(userId, name = "pk_user_privacy_settings")

    init {
        index("idx_user_id", true, userId)
    }
}

enum class PrivacyVisibility {
    PUBLIC, PRIVATE, FRIENDS_ONLY
}


/**
 * Represents the user status table in the database.
 * This table stores the current status of the user (e.g., online, offline)
 * and the last time the status was updated.
 * The `userId` field is a foreign key referencing the `User` table,
 * ensuring that each status is associated with a valid user.
 * The `status` field is a string that indicates the user's current status,
 * defaulting to "offline".
 * The `lastUpdated` field stores the timestamp of the last status update,
 * defaulting to the current time when the record is created.
 * This structure allows for efficient tracking of user statuses,
 * enabling features such as online presence indicators and activity tracking.
 * @property userId The unique identifier for the user, referencing the `User` table.
 * @property status The current status of the user, defaulting to "offline".
 * @property lastUpdated The timestamp of the last status update, defaulting to the current time.
 * @see UserTable The table storing user information, which this table references.
 */
object UserStatusTable : Table("user_status") {
    val userId = varchar("pk_user_id", 255).references(
        uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val status = varchar("status", 255).default(ActiveStatus.OFFLINE.name)
    val lastUpdated = long("last_updated").default(System.currentTimeMillis())

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(userId, name = "pk_user_status")

    init {
        index("idx_user_id", true, userId)
    }
}

enum class ActiveStatus {
    ONLINE, OFFLINE
}


/**
 * Represents the user devices table in the database.
 * This table stores information about devices that users are logged into,
 * including device IDs, names, public keys, encrypted key material,
 * and the last time the device was used.
 * The `userId` field is a foreign key referencing the `User` table,
 * ensuring that each device is associated with a valid user.
 * The `deviceId` field is unique for each user-device pair,
 * allowing for efficient tracking of multiple devices per user.
 * The `devicePublicKey` field stores the public key of the device,
 * while `encryptedKeyMaterial` stores sensitive data encrypted with the user's master key.
 * The `keyDerivationSalt` is used for key derivation purposes.
 * The `lastUsedAt` field stores the timestamp of the last time the device was used,
 * defaulting to the current time when the record is created.
 * This structure allows for efficient management of user devices,
 * enabling features such as device management and security controls.
 * @property userId The unique identifier for the user, referencing the `User` table.
 * @property deviceId The unique identifier for the device, specific to each user.
 * @property deviceName The name of the device, e.g., "John's iPhone".
 * @property devicePublicKey The public key of the device, used for secure communication.
 * @property encryptedKeyMaterial Encrypted key material, stored securely.
 * @property keyDerivationSalt Salt used for key derivation, enhancing security.
 * @property lastUsedAt The timestamp of the last time the device was used, defaulting to the current time.
 * @see UserTable The table storing user information, which this table references.
 */
object UserDevicesTable : Table("user_logged_in_devices") {
    val userId = varchar("pk_user_id", 255).references(
        UserTable.uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val deviceId = varchar("device_id", 255)
    val deviceName = varchar("device_name", 255)
    val devicePublicKey = varchar("device_public_key", 255).default("")
    val encryptedKeyMaterial = varchar("encrypted_key_material", 255).default("")
    val keyDerivationSalt = varchar("key_derivation_salt", 64).default("")
    val lastUsedAt = long("last_used_at").default(System.currentTimeMillis())

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(userId, deviceId, name = "pk_user_logged_in_devices")

    init {
        index("idx_user_id", true, userId)
        index("idx_device_id", true, deviceId)
        index("idx_last_used_at", false, lastUsedAt)
        uniqueIndex("idx_unique_device_per_user", userId, deviceId)
    }
}


/**
 * Represents the friendship status in the application.
 * This enum defines the possible states of a friendship between users,
 * allowing for clear and consistent management of friendship relationships.
 * The statuses include:
 * - PENDING: A friend request has been sent but not yet accepted or declined.
 * - ACCEPTED: The friend request has been accepted, establishing a friendship.
 * - DECLINED: The friend request was declined by the receiver.
 * - BLOCKED: One user has blocked the other, preventing further interaction.
 * - CANCELLED: The friend request was cancelled by the requester before a response was received.
 */
enum class FriendshipRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    BLOCKED
}

/**
 * Represents the friends table in the database.
 * This table stores friendship records between users,
 * including the requester and receiver user IDs,
 * the status of the friendship,
 * and timestamps for when the friendship was requested,
 * responded to, created, and updated.
 * The `id` field is a unique identifier for each friendship record,
 * which is a UUID.
 * The `requesterId` and `receiverId` fields are foreign keys referencing the `User` table,
 * ensuring that each friendship is associated with valid users.
 * The `status` field indicates the current status of the friendship,
 * defaulting to "PENDING".
 * The `requestedAt`, `respondedAt`, `createdAt`, and `updatedAt` fields store timestamps
 * for various friendship activities.
 * This structure allows for efficient management of friendships,
 * enabling features such as friend requests, acceptance, and status tracking.
 * @property id The unique identifier for the friendship record, a UUID.
 * @property requesterId The unique identifier for the user who sent the friend request, referencing the `User` table.
 * @property receiverId The unique identifier for the user who received the friend request, referencing the `User` table.
 * @property status The current status of the friendship, defaulting to "PENDING".
 * @property requestedAt The timestamp when the friend request was made, defaulting to the current time.
 * @property respondedAt The timestamp when the friend request was responded to, nullable.
 * @property createdAt The timestamp when the friendship record was created, defaulting to the current time.
 * @property updatedAt The timestamp when the friendship record was last updated, nullable.
 * @see UserTable The table storing user information, which this table references.
 * @see FriendshipRequestStatus The enum representing the possible statuses of a friendship.
 *
 */
object FriendsRequestTable : Table("friends_request_db") {
    val id = varchar("pk_friend_id", 255)
    val requesterId = varchar("requester_id", 255).references(
        uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val receiverId = varchar("receiver_id", 255).references(
        uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val status = varchar("status", 30).default(FriendshipRequestStatus.PENDING.name)
    val requestedAt = long("requested_at").default(System.currentTimeMillis())
    val respondedAt = long("responded_at").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id, name = "pk_request_friend_id")

    init {
        // Prevent duplicate friend requests (both directions)
        uniqueIndex("idx_unique_friendship", requesterId, receiverId)

        // Indexes for efficient querying
        index("idx_requester", false, requesterId)
        index("idx_receiver", false, receiverId)
        index("idx_status", false, status)
        index("idx_requested_at", false, requestedAt)

        // Composite indexes for common query patterns
        index("idx_requester_status", false, requesterId, status)
        index("idx_receiver_status", false, receiverId, status)

    }
}


object UserFriendsTable : Table("user_friends") {
    val id = varchar("pk_friendship_id", 255)
    val userId = varchar("pk_user_id", 255).references(
        UserTable.uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val friendId = varchar("friend_id", 255).references(
        UserTable.uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val createdAt = long("created_at").default(System.currentTimeMillis())
    val chatRoomPath = varchar("chat_room_path", 255)
        .nullable()
        .default(null)
    val friendshipRequestStatus = varchar("friendship_status", 30)
        .default(FriendshipRequestStatus.PENDING.name)
    val isMuted = bool("is_muted").default(false)
    val isFavorite = bool("is_favorite").default(false)
    val lastMessage = varchar("last_message", 255).nullable().default(null)
    val lastMessageAt = long("last_message_at").nullable().default(null)


    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id, name = "pk_user_friends_id")

    init {
        index("idx_chat_room_path", false, chatRoomPath)
        // Unique index to prevent duplicate friendships
        uniqueIndex("idx_unique_friendship", userId, friendId)
        // Composite index for common queries
        index("idx_user_friend", false, userId, friendId)
        index("idx_user_friend_status", false, userId, friendshipRequestStatus, isMuted, isFavorite)
        index("idx_friend_status", false, friendId, friendshipRequestStatus, isMuted, isFavorite)
    }
}


object UserNotificationTokenTable : Table("user_notification_token") {
    val userId = varchar("pk_user_id", 255).references(
        UserTable.uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val token = varchar("token", 255).uniqueIndex()
    val createdAt = long("created_at").default(System.currentTimeMillis())
    val updatedAt = long("updated_at").default(System.currentTimeMillis())

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(userId, name = "pk_user_notifications_token")

    init {
        index("idx_user_id", true, userId)
    }
}
