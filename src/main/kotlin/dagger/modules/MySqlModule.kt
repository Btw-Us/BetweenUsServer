package com.aatech.dagger.modules

import com.aatech.data.mysql.repository.user.FriendsRepository
import com.aatech.data.mysql.repository.user.UserPrivacySettingsRepository
import com.aatech.data.mysql.repository.user.UserRepository
import com.aatech.data.mysql.repository.user.UserStatusRepository
import com.aatech.data.mysql.repository.user.impl.UserPrivacySettingsRepositoryImp
import com.aatech.data.mysql.repository.user.impl.UserRepositoryImp
import com.aatech.data.mysql.repository.user.impl.UserStatusTableRepositoryImp
import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import com.aatech.database.mysql.repository.auth_token.imp.AuthTokenRepositoryImp
import com.aatech.database.mysql.repository.user.impl.FriendsRepositoryImp
import com.aatech.plugin.configureMySqlDatabases
import dagger.Module
import dagger.Provides
import org.jetbrains.exposed.v1.jdbc.Database
import javax.inject.Singleton

@Module
class MySqlModule {

    @Provides
    @Singleton
    fun provideMySQLDatabase(): Database = configureMySqlDatabases()

    @Provides
    @Singleton
    fun provideAuthTokenRepository(): AuthTokenRepository =
        AuthTokenRepositoryImp()

    @Provides
    @Singleton
    fun provideFriendsRepository(): FriendsRepository =
        FriendsRepositoryImp()

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository =
        UserRepositoryImp()

    @Provides
    @Singleton
    fun provideUserPrivacySettingsRepository(): UserPrivacySettingsRepository =
        UserPrivacySettingsRepositoryImp()

    @Provides
    @Singleton
    fun provideUserStatusRepository(): UserStatusRepository =
        UserStatusTableRepositoryImp()
}