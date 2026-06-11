package com.pulsenet.mobile

import com.pulsenet.mobile.data.IdentityManager
import org.junit.Test
import org.junit.Assert.*

class IdentityTest {
    @Test
    fun test_signature_verification() {
        val identityManager = IdentityManager()
        val data = "Hello PulseNet"
        val signature = identityManager.sign(data)
        val publicKey = identityManager.getPublicKey()

        assertTrue(identityManager.verify(data, signature, publicKey))
    }

    @Test
    fun test_invalid_signature() {
        val identityManager = IdentityManager()
        val data = "Hello PulseNet"
        val publicKey = identityManager.getPublicKey()

        // Use a valid base64 but incorrect signature
        val invalidSig = "dmFsaWRfYmFzZTY0"
        assertFalse(identityManager.verify(data, invalidSig, publicKey))
    }
}
