/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: 7/20/25, 6:11 PM
 * Author: ayaan
 * Project: BetweenUsServer
 *
 * No part of this work may be reproduced, distributed, or transmitted in any form
 * or by any means, including photocopying, recording, or other electronic or
 * mechanical methods, without the prior written permission of the copyright holder.
 *
 * For permission requests, contact: ayaan35200@gmail.com
 */
package com.aatech.config.api_config

import io.ktor.http.HttpStatusCode
import com.aatech.config.response.ErrorResponse
import kotlinx.serialization.Serializable

/**
 * Define the type of client making the request.
 * It can be used to differentiate between different types of clients
 * such as web, mobile, or desktop applications.
 */
enum class ClientType{
    WEB,
    MOBILE,
    DESKTOP,
    API,
    OTHER
}

/**
 * Represents the authentication parameters required for API requests.
 * This class encapsulates the necessary information for authenticating
 * a user or client when making API calls.
 *
 * @property clientType The type of client making the request (e.g., WEB, MOBILE).
 * @property authToken The authentication token used to verify the client's identity.
 * @property userId The unique identifier of the user making the request (optional).
 * @property clientVersion The version of the client application (optional).
 * @property deviceId The unique identifier of the device making the request (optional).
 * @property session The session identifier for the current user session (optional).
 * @property deviceModel The name of the device making the request (optional).
 * This class provides a method to validate the authentication parameters
 * to ensure that the API key is not empty and the client type is recognized.
 * * @constructor Creates an instance of [AuthenticationParams] with the specified parameters.
 * * @property clientType The type of client making the request (default is API).
 */
@Serializable
data class AuthenticationParams(
    val clientType: ClientType = ClientType.API,
    val authToken: String,
    val userId: String? = null,
    val clientVersion : String? = null,
    val deviceId: String? = null,
    val session : String? = null,
    val deviceModel: String? = null,

    ){
    /**
     * Validates the authentication parameters.
     * This method checks if the token is not empty and the client type is recognized.
     *
     * @return true if the parameters are valid, false otherwise.
     */
    fun isAuthenticationParams(): ErrorResponse? {
        if( authToken.isEmpty() || authToken == "null" ) {
            return ErrorResponse(
                errorCode = HttpStatusCode.Unauthorized.value,
                errorMessage = "Authentication token is missing",
                details = "The authentication token is required for this request."
            )
        }
        if (clientType == ClientType.OTHER) {
            return ErrorResponse(
                errorCode = HttpStatusCode.BadRequest.value,
                errorMessage = "Invalid client type provided",
                details = "The client type provided is not recognized or is invalid."
            )
        }
        return null
    }

    fun isUserLoggedIn(): Boolean {
        return userId != null && userId.isNotEmpty()
    }

    /**
     * Converts the authentication parameters to a map of headers.
     * This method is useful for including the authentication information
     * in HTTP requests.
     *
     * @return A map containing the headers required for authentication.
     */
    fun AuthenticationParams.convertToHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $authToken"
        headers["X-Client-Type"] = clientType.name
        userId?.let { headers["X-User-Id"] = it }
        clientVersion?.let { headers["X-Client-Version"] = it }
        deviceId?.let { headers["X-Device-Id"] = it }
        session?.let { headers["X-Session"] = it }
        deviceModel?.let { headers["X-Device-Name"] = it }
        return headers
    }
}
