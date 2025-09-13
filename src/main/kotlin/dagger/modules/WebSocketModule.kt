/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: September 01, 2025 12:38 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.dagger.modules

import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.websocket.cm.PersonalChatConnectionManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class WebSocketModule {
    @Provides
    @Singleton
    fun provideWebSocketJson() = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideWebSocketConnectionManager(
        personalChatRepository: PersonChatRepository,
        json: kotlinx.serialization.json.Json
    ): PersonalChatConnectionManager =
        PersonalChatConnectionManager(
            personalChatRepository = personalChatRepository,
            json = json
        )

    @Provides
    @Singleton
    fun provideAllMessageConnectionManager(
        personalChatRepository: PersonChatRepository,
        json: kotlinx.serialization.json.Json
    ): com.aatech.websocket.cm.AllMessageConnectionManager =
        com.aatech.websocket.cm.AllMessageConnectionManager(
            personalChatRepository = personalChatRepository,
            json = json
        )
}