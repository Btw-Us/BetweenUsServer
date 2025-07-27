package com.aatech.plugin

import com.aatech.config.response.createErrorResponse
import com.aatech.utils.getEnv
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun AuthenticationConfig.configureJWTAuthentication(
) {
    val config = JWTConfig()
    jwt("jwt-auth") {
        realm = config.realm
        val jwtVerifier = JWT
            .require(Algorithm.HMAC256(config.secret))
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .build()

        verifier(jwtVerifier)
        validate { credential ->
            if (credential.payload.audience.contains(config.audience)) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }
        challenge { _, _ ->
            call.respond(
                status = io.ktor.http.HttpStatusCode.Unauthorized,
                message = createErrorResponse(
                    code = io.ktor.http.HttpStatusCode.Unauthorized.value,
                    message = "Unauthorized access. Please provide a valid JWT token.",
                    details = """
                            The request was denied due to missing or invalid authentication credentials.
                            Please include a valid JWT token in the request header:
                            Example:
                            Authorization: Bearer <your_jwt_token_here>
                            If you do not have a token, please contact the system administrator to obtain one.
                        """.trimIndent()
                )
            )
        }
    }
    configureGoogleAuth()
}


data class JWTConfig(
    val realm: String = getEnv("JWT_REALM"),
    val secret: String = getEnv("JWT_SECRET"),
    val issuer: String = getServerURLWithPort(),
    val audience: String = getServerURLWithPort(),
    val expiration: Long = getEnv("JWT_EXPIRATION").toLongOrNull() ?: 3600L // Default to 1 hour if not set
)


fun getServerURLWithPort(): String {
    val host = getEnv("SERVER_HOST", "localhost")
    val port = getEnv("SERVER_PORT", "8080")
    return "http://$host:$port"
}