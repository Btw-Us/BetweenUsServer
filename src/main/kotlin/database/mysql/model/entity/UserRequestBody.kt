/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 15, 2025 01:00 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.mysql.model.entity

import com.aatech.database.mysql.model.FriendshipRequestStatus
import kotlinx.serialization.Serializable

@Serializable
data class SendFriendRequestBody(
    val requesterId: String,
    val receiverId: String
)

@Serializable
data class ChangeFriendRequestStatusBody(
    val userId: String,
    val friendId: String,
    val requestStatus: FriendshipRequestStatus
)

@Serializable
data class UserFirebaseToken(
    val userId: String,
    val token: String
)