/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/21/25, 10:16 PM
 * Created by: ayaan
 *
 */

package com.aatech.database.mysql.repository.user

import com.aatech.database.mysql.model.FriendshipStatus
import com.aatech.database.mysql.model.entity.Friend

interface FriendsRepository {
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