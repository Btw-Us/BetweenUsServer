package com.aatech.dagger.components

import com.aatech.dagger.modules.MySqlModule
import com.aatech.data.mysql.repository.user.FriendsRepository
import com.aatech.data.mysql.repository.user.UserRepository
import com.aatech.data.mysql.repository.user.UserStatusRepository
import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import com.aatech.database.mysql.services.AuthTokenService
import dagger.Component
import org.jetbrains.exposed.v1.jdbc.Database
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MySqlModule::class
    ]
)
interface MySqlComponent {
    fun getMySqlDatabase(): Database

    fun getAuthTokenRepository(): AuthTokenRepository
    fun getFriendsRepository(): FriendsRepository
    fun getUserRepository(): UserRepository
    fun getUserStatusRepository(): UserStatusRepository

    fun getAuthTokenService(): AuthTokenService

}