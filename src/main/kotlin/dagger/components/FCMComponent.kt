/*
 * Copyright © 2025 ayaan. All rights reserved.
 *
 * This work is the exclusive property of ayaan.
 *
 * Created: August 20, 2025 08:43 PM
 * Author: ayaan
 * Project: BetweenUsServe
 *
 * This software is provided "as is", without warranty of any kind, express or implied.
 * You are free to use, modify, and distribute this work for any purpose.
 *
 * For questions or contributions, contact: ayaan35200@gmail.com
 */

package com.aatech.dagger.components

import com.aatech.dagger.modules.FCMModule
import com.aatech.fcm.SendMessageService
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        FCMModule::class
    ]
)
interface FCMComponent {


    fun getSendMessageService(): SendMessageService
}