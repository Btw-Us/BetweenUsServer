/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 27, 2025 01:25 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.config.response

import com.aatech.database.mysql.model.entity.User
import kotlinx.serialization.Serializable

@Serializable
class AllFriendsResponse(
    val user: User,
    val chatRoomPath: String?
)