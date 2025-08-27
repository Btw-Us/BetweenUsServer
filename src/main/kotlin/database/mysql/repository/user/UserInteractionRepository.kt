/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 13, 2025 09:02 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.mysql.repository.user

import com.aatech.config.response.AllFriendsResponse
import com.aatech.database.mysql.model.FriendshipRequestStatus
import com.aatech.database.mysql.model.entity.SearchUserResponse
import com.aatech.database.mysql.model.entity.User

interface UserInteractionRepository {
    suspend fun findFriends(
        loggedUserId: String,
        userName: String,
    ): List<SearchUserResponse>

    suspend fun sendOrUnsendFriendRequest(
        userId: String,
        friendId: String,
    ): FriendshipAction

    enum class FriendshipAction {
        SEND,
        UNSEND
    }

    suspend fun getAllFriends(
        userId: String,
    ): List<AllFriendsResponse>

    suspend fun getAllReceivedRequests(
        userId: String,
    ): List<SearchUserResponse>

    suspend fun getAllSentRequests(
        userId: String,
    ): List<SearchUserResponse>

    suspend fun responseToFriendRequest(
        userId: String,
        friendId: String,
        requestStatus: FriendshipRequestStatus
    ): FriendshipRequestStatus


    suspend fun getUserById(
        userId: String
    ): User?

    suspend fun getUserTokenById(
        userId: String
    ): String?


    suspend fun insertChatRoomId(
        userId: String,
        friendId: String,
        chatRoomId: String
    ): Boolean

    suspend fun checkHasChatRoomId(
        userId: String,
        friendId: String
    ): String?
}