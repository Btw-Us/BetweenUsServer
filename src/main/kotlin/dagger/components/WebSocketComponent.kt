/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: September 01, 2025 12:39 AM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.dagger.components

import com.aatech.dagger.modules.MongoDbModule
import com.aatech.dagger.modules.WebSocketModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        WebSocketModule::class,
        MongoDbModule::class
    ]
)
interface WebSocketComponent {

    fun provideWebSocketConnectionManager(): com.aatech.websocket.cm.PersonalChatConnectionManager

    fun provideWebSocketJson(): kotlinx.serialization.json.Json


}