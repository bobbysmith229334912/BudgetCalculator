package com.hardcoreamature.budgetcalculatoreno

import android.util.Base64
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    fun generateApiSignature(apiSecret: String, path: String, nonce: String, postData: String): String {
        val sha256Hasher = MessageDigest.getInstance("SHA-256")
        val noncePostData = nonce + postData
        val hash = sha256Hasher.digest(noncePostData.toByteArray(Charsets.UTF_8))

        val secretDecoded = Base64.decode(apiSecret, Base64.NO_WRAP)
        val mac = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(secretDecoded, "HmacSHA512")
        mac.init(secretKey)

        val pathBytes = path.toByteArray(Charsets.UTF_8)
        val combined = ByteBuffer.allocate(pathBytes.size + hash.size)
        combined.put(pathBytes)
        combined.put(hash)
        val macData = mac.doFinal(combined.array())

        return Base64.encodeToString(macData, Base64.NO_WRAP)
    }
}
