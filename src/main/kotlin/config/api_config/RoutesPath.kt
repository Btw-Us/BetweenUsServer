package com.aatech.config.api_config

const val CURRENT_VERSION = "v1"
const val BASE_REST_PATH = "/api/$CURRENT_VERSION"
const val BASE_WP_PATH = "ws/$CURRENT_VERSION"
const val HEALTH = "/health"
private const val AUTH = "$BASE_REST_PATH/auth"
private const val USER = "$AUTH/user"


sealed class AuthRoutes(val path: String) {
    object GenerateToken : AuthRoutes("$AUTH/generate-token")
    object ValidateToken : AuthRoutes("$AUTH/validate-token")
    object DeleteToken : AuthRoutes("$AUTH/delete-token")
}


sealed class PersonalChatRoutes(val path: String) {
    object CreateChat : PersonalChatRoutes("$BASE_REST_PATH/personal-chats/create")
    object GetAllChats : PersonalChatRoutes("$BASE_WP_PATH/personal-chats/get-all")
}


sealed class LoginRoutes(val path: String) {
    object LogInWithOAuth : LoginRoutes("$USER/login")
    object OAuthLoginCallback : LoginRoutes("/callback")
    object LogInWithGoogle : LoginRoutes("$USER/login/google")
    object CheckUserNameAvailability : LoginRoutes("$USER/check-username-availability")
    object SetUpUserProfile : LoginRoutes("${LogInWithOAuth.path}/setup-profile")
    object CheckPassword : LoginRoutes("${LogInWithOAuth.path}/check-password")
    object LogOut : LoginRoutes("$USER/logout")
}