/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: July 30, 2025 10:35 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.plugin

import com.aatech.config.api_config.LoginRoutes
import com.aatech.utils.getEnv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun AuthenticationConfig.configureGoogleAuth(
) {
    val oAuthClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Ignore unknown keys in JSON
            })
        }
    }
    oauth("auth-oauth-google") {
        client = oAuthClient
        urlProvider = {
            "${getServerURLWithPort()}${LoginRoutes.OAuthLoginCallback.path}"
        }
        providerLookup = {
            OAuthServerSettings.OAuth2ServerSettings(
                name = "google",
                authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                accessTokenUrl = "https://oauth2.googleapis.com/token",
                requestMethod = HttpMethod.Post,
                clientId = getEnv("GOOGLE_CLIENT_ID"),
                clientSecret = getEnv("GOOGLE_CLIENT_SECRET"),
                defaultScopes = listOf(
                    "openid",
                    "https://www.googleapis.com/auth/userinfo.email",
                    "https://www.googleapis.com/auth/userinfo.profile"
                ),
                extraAuthParameters = listOf(
                    "access_type" to "offline",
                    "prompt" to "consent"
                )
            )
        }
    }
}


@Serializable
data class UserInfo(
    val sub: String,
    val name: String? = null,
    val picture: String? = null,
    val email: String? = null,
    @SerialName("email_verified")
    val emailVerified: Boolean? = null,
    val locale: String? = null
)


suspend fun getUserInfo(accessToken: String): UserInfo? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    return try {
        // Debug: Print the token (first few and last few characters for security)
        println("Using token: ${accessToken.take(10)}...${accessToken.takeLast(10)}")

        val response: HttpResponse = client.get("https://www.googleapis.com/oauth2/v3/userinfo") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
                // Add User-Agent header (sometimes required)
                append(HttpHeaders.UserAgent, "YourApp/1.0")
            }
        }

        println("Response status: ${response.status}")
        println("Response headers: ${response.headers}")

        if (response.status.isSuccess()) {
            response.body<UserInfo>()
        } else {
            // Print response body for more details about the error
            val errorBody = response.bodyAsText()
            println("Error body: $errorBody")
            null
        }
    } catch (e: Exception) {
        println("Exception: ${e.message}")
        e.printStackTrace()
        null
    } finally {
        client.close()
    }
}


suspend fun fetchUserInfo(accessToken: String): UserInfo {
    val oAuthClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.NONE
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(accessToken, "")
                }
            }
        }
    }

    return try {
        val response = oAuthClient.get("https://www.googleapis.com/oauth2/v3/userinfo") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
                append(HttpHeaders.Accept, "application/json")
            }
        }


        println("Response status: ${response.status}")

        when {
            response.status.isSuccess() -> {
                response.body<UserInfo>()
            }

            response.status == HttpStatusCode.Unauthorized -> {
                val errorBody = response.bodyAsText()
                throw Exception("Invalid or expired access token: $errorBody")
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Google API returned ${response.status}: $errorBody")
            }
        }
    } catch (e: Exception) {
        println("Exception in fetchUserInfo: ${e.message}")
        throw e
    } finally {
        oAuthClient.close()
    }
}