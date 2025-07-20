/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: 7/20/25, 4:33 PM
 * Author: ayaan
 * Project: BetweenUsServer
 *
 * No part of this work may be reproduced, distributed, or transmitted in any form
 * or by any means, including photocopying, recording, or other electronic or
 * mechanical methods, without the prior written permission of the copyright holder.
 *
 * For permission requests, contact: ayaan35200@gmail.com
 */

package com.aatech.config.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val errorCode: Int,
    val errorMessage: String,
    val details: String? = null
)

fun createErrorResponse(
    code: Int,
    message: String,
    details: String? = null
): ErrorResponse =
    ErrorResponse(
        errorCode = code,
        errorMessage = message,
        details = details
    )


