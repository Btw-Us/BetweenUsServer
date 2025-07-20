package com.aatech.config.api_config

const val CURRENT_VERSION = "v1"
const val BASE_REST_PATH = "/api/$CURRENT_VERSION"
const val BASE_WP_PATH = "ws/$CURRENT_VERSION"
const val HEALTH = "/health"
private const val AUTH = "$BASE_REST_PATH/auth"


sealed class AuthRoutes(val path: String) {
    object GenerateToken : AuthRoutes("$AUTH/generate-token")
    object ValidateToken : AuthRoutes("$AUTH/validate-token")
    object DeleteToken : AuthRoutes("$AUTH/delete-token")
}


