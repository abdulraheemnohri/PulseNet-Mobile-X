package com.pulsenet.mobile.data

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64

class IdentityManager {
    private val keyPair: KeyPair by lazy {
        val kpg = KeyPairGenerator.getInstance("Ed25519")
        kpg.generateKeyPair()
    }

    fun getPublicKey(): String {
        return Base64.getEncoder().encodeToString(keyPair.public.encoded)
    }

    fun sign(data: String): String {
        val sig = Signature.getInstance("Ed25519")
        sig.initSign(keyPair.private)
        sig.update(data.toByteArray())
        return Base64.getEncoder().encodeToString(sig.sign())
    }

    fun verify(data: String, signature: String, publicKeyEncoded: String): Boolean {
        return try {
            val sig = Signature.getInstance("Ed25519")
            val kf = java.security.KeyFactory.getInstance("Ed25519")
            val publicKey = kf.generatePublic(java.security.spec.X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyEncoded)))
            sig.initVerify(publicKey)
            sig.update(data.toByteArray())
            sig.verify(Base64.getDecoder().decode(signature))
        } catch (e: Exception) {
            false
        }
    }
}
