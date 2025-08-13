package com.aatech.dagger.modules

import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import com.aatech.database.mysql.repository.auth_token.imp.AuthTokenRepositoryImp
import com.aatech.database.mysql.repository.user.UserLogInRepository
import com.aatech.database.mysql.repository.user.impl.UserLogInRepositoryImp
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
    fun provideUserRepository(): UserLogInRepository =
        UserLogInRepositoryImp()

}