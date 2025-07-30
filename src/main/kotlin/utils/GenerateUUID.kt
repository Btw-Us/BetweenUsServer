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

package com.aatech.utils

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

private val NAMESPACE_UUID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8") // DNS namespace


fun String.generateUuidFromSub(): UUID =
    generateUuidV5(this)


private fun generateUuidV5(name: String): UUID {
    val nameBytes = name.toByteArray(Charsets.UTF_8)
    val namespaceBytes = uuidToBytes()

    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(namespaceBytes)
    digest.update(nameBytes)
    val hash = digest.digest()

    // Set version (5) and variant bits
    hash[6] = (hash[6].toInt() and 0x0F or 0x50).toByte() // Version 5
    hash[8] = (hash[8].toInt() and 0x3F or 0x80).toByte() // Variant bits

    return bytesToUuid(hash)
}

private fun uuidToBytes(): ByteArray {
    val bb = ByteBuffer.allocate(16)
    bb.putLong(NAMESPACE_UUID.mostSignificantBits)
    bb.putLong(NAMESPACE_UUID.leastSignificantBits)
    return bb.array()
}

private fun bytesToUuid(bytes: ByteArray): UUID {
    val bb = ByteBuffer.wrap(bytes)
    val mostSigBits = bb.long
    val leastSigBits = bb.long
    return UUID(mostSigBits, leastSigBits)
}