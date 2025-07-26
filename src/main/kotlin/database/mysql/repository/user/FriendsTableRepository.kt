/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 10:16 PM
 * Created by: ayaan
 *
 */

package com.aatech.data.mysql.repository.user

import com.aatech.data.mysql.model.Friend
import com.aatech.data.mysql.model.FriendshipStatus
import com.aatech.data.mysql.model.UserStatus

interface FriendsTableRepository {
    suspend fun addFriend(
        userId: String,
        friendId: String
    ): Boolean

    suspend fun removeFriend(
        userId: String, friendId: String
    ): Boolean

    suspend fun getFriendsList(
        userId: String,
        friendShipStatus: FriendshipStatus = FriendshipStatus.ACCEPTED,
    ): List<Friend> // Returns a list of friend IDs

    suspend fun isFriend(
        userId: String, friendId: String
    ): Boolean
}