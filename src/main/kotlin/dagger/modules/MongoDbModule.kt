/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 10, 2025 02:13 PM
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
import com.aatech.database.mongodb.repository.impl.PersonChatRepositoryImp
import com.aatech.plugin.configureMongoDB
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MongoDbModule {

    @Provides
    @Singleton
    fun provideMongoDatabase(): MongoDatabase {
        return configureMongoDB()
    }


    @Provides
    @Singleton
    fun providePersonChatRepository(
        database: MongoDatabase
    ): PersonChatRepository {
        return PersonChatRepositoryImp(database)
    }

}