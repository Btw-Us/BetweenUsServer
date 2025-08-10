/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 10, 2025 02:17 PM
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
import com.aatech.database.mongodb.repository.PersonChatRepository
import com.aatech.websocket.cm.PersonChatRoomConnectionManager
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MongoDbModule::class
    ]
)
interface MongoDbComponent {
    fun getMongoDbDatabase(): MongoDatabase

    fun getPersonChatRepository(): PersonChatRepository

    fun getPersonChatRoomConnectionManager(): PersonChatRoomConnectionManager
}