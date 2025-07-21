/*
 * Copyright (c) 2025 ayaan. All rights reserved.
 *
 * This file is part of BetweenUsServe.
 *
 * Created at: 7/22/25, 1:18 AM
 * Created by: ayaan
 *
 */

package com.aatech.data.mangodb.repository

import com.aatech.data.mangodb.model.PersonalChatModel

interface PersonChatRepository {
    suspend fun createChat(model : PersonalChatModel): String

    suspend fun getAllChats(userId: String, limit: Int = 50, offset: Int = 0): List<PersonalChatModel>
}