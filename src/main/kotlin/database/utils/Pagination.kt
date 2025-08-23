/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 23, 2025 11:29 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.database.utils

import kotlinx.serialization.Serializable

@Serializable
data class PaginationRequest(
    val page: Int = 1,
    val size: Int = 20,
) {
    val offset: Int get() = (page - 1) * size
    val limit: Int get() = size

    init {
        require(page >= 1) { "Page must be >= 1" }
        require(size in 1..100) { "Size must be between 1 and 100" }
    }
}

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

@Serializable
data class PaginationInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)