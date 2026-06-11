package com.pulsenet.mobile.util

import java.security.MessageDigest
import java.util.Base64

object HashUtils {
    fun sha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun generateCID(content: String, author: String, timestamp: Long): String {
        val raw = "$author:$timestamp:$content"
        return "cid-${sha256(raw).take(16)}"
    }
}
