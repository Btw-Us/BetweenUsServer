/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 12:52 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.model

import com.aatech.data.mysql.model.UserPrivacySettingsTable.allowLastSeen
import com.aatech.data.mysql.model.UserPrivacySettingsTable.allowProfilePicture
import com.aatech.data.mysql.model.UserPrivacySettingsTable.allowReadReceipts
import com.aatech.data.mysql.model.UserPrivacySettingsTable.userId
import com.aatech.data.mysql.model.UserStatusTable.lastUpdated
import com.aatech.data.mysql.model.UserStatusTable.status
import com.aatech.data.mysql.model.UserStatusTable.userId
import com.aatech.data.mysql.model.UserTable.createdAt
import com.aatech.data.mysql.model.UserTable.email
import com.aatech.data.mysql.model.UserTable.fullName
import com.aatech.data.mysql.model.UserTable.lastLogin
import com.aatech.data.mysql.model.UserTable.passwordHash
import com.aatech.data.mysql.model.UserTable.profilePicture
import com.aatech.data.mysql.model.UserTable.updatedAt
import com.aatech.data.mysql.model.UserTable.username
import com.aatech.data.mysql.model.UserTable.uuid
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
 *  @property passwordHash The hashed password of the user.
 *  @property createdAt The timestamp when the user was created.
 *  @property updatedAt The timestamp when the user was last updated, nullable.
 *  @property lastLogin The timestamp of the user's last login, nullable.
 *  @see UserPrivacySettingsTable The table storing user-specific privacy settings.
 *  @see UserStatusTable The table storing the current status of the user and the last time it was updated.
 */
object UserTable : Table("user_db") {
    val uuid = long("pk_user_id").autoIncrement()
    val username = varchar("username", 255).uniqueIndex()
    val fullName = varchar("full_name", 255)
        .nullable().default("Unknown User").check { it.isNotNull() }
    val email = varchar("email", 255).uniqueIndex()
    val profilePicture = varchar("profile_picture", 255).nullable()
    val passwordHash = varchar("password", 255)
    val createdAt = long("created_at").default(System.currentTimeMillis())
    val updatedAt = long("updated_at").nullable()
    val lastLogin = long("last_login").nullable()

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(uuid, name = "pk_user_id")

    init {
        index("idx_username", true, username)
        index("idx_email", true, email)
        index("idx_password", false, passwordHash)
    }
}


data class User(
    val uuid: Long,
    val username: String,
    val fullName: String?,
    val email: String,
    val profilePicture: String?,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val lastLogin: Long? = null
)

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
    val userId = long("user_id").references(
        UserTable.uuid,
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

data class UserPrivacySettings(
    val userId: Long,
    val allowProfilePicture: PrivacyVisibility = PrivacyVisibility.PUBLIC,
    val allowLastSeen: PrivacyVisibility = PrivacyVisibility.PUBLIC,
    val allowReadReceipts: Boolean = true
)

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
    val userId = long("user_id").references(
        UserTable.uuid,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val status = varchar("status", 255).default(ActiveStatus.OFFLINE.name)
    val lastUpdated = long("last_updated").default(System.currentTimeMillis())

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(userId, name = "pk_user_status")

    init {
        index("idx_user_id", true, userId)
    }
}

enum class ActiveStatus {
    ONLINE, OFFLINE
}

data class UserStatus(
    val userId: Long,
    val status: String = ActiveStatus.OFFLINE.name,
    val lastUpdated: Long = System.currentTimeMillis()
)

