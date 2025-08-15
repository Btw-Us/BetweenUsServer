/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 15, 2025 12:03 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.mysql.model.entity

import com.aatech.database.mysql.model.FriendshipStatus
import kotlinx.serialization.Serializable

@Serializable
data class SearchUserResponse(
    val user: User,
    val friendshipStatus: FriendshipStatus? = null
)