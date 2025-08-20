/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 20, 2025 07:02 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.plugin

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*

fun Application.configureFirebaseCloudMessage() {
    try {
        val serviceAccountStream = this::class.java.classLoader.getResourceAsStream("serviceAccountKey.json")
        val option = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
            .build()
        FirebaseApp.initializeApp(option)
    } catch (e: Exception) {
        System.err.println(
            "Failed to initialize Firebase Cloud Messaging: ${e.message}"
        )
    }
}