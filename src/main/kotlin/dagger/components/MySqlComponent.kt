package com.aatech.dagger.components

import com.aatech.dagger.modules.MySqlModule
import com.aatech.database.mysql.repository.auth_token.AuthTokenRepository
import com.aatech.database.mysql.repository.user.UserInteractionRepository
import com.aatech.database.mysql.repository.user.UserLogInRepository
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
    fun getUserRepository(): UserLogInRepository
    fun getAuthTokenService(): AuthTokenService
    fun getUserInteractionRepository(): UserInteractionRepository

}