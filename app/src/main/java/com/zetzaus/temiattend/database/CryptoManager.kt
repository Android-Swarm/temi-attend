package com.zetzaus.temiattend.database

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.zetzaus.temiattend.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object CryptoManager {
    private val alias = "${BuildConfig.APPLICATION_ID}.${CryptoManager::class.simpleName}"
    private const val algorithm = "AES/CBC/PKCS7Padding"
    private const val provider = "AndroidKeyStore"

    private val decryptKey: SecretKey

    init {
        val keystore = KeyStore.getInstance(provider).apply { load(null) }

        if (!keystore.containsAlias(alias)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider)
            val keySpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            }.build()

            keyGenerator.run {
                init(keySpec)
                generateKey()
            }
        }

        decryptKey = keystore.getKey(alias, CharArray(0)) as SecretKey
    }

    /**
     * Encrypts a [String].
     *
     * @param message The [String] to encrypt.
     * @return A pair of [String] object. The first [String] is the cipher text. The second [String]
     * is the initialization vector.
     */
    fun encrypt(message: String): Pair<String, String> {
        val encryptCipher =
            Cipher.getInstance(algorithm).apply { init(Cipher.ENCRYPT_MODE, decryptKey) }

        return Pair(
            encryptCipher.doFinal(message.toByteArray()).encodeBase64(),
            encryptCipher.iv.encodeBase64()
        )
    }

    /**
     * Decrypts a cipher [String] using the given initialization vector.
     *
     * @param cipher The cipher text.
     * @param iv The initialization vector used when encrypting the original [String].
     */
    fun decrypt(cipher: String, iv: String) =
        String(
            Cipher.getInstance(algorithm).apply {
                init(Cipher.DECRYPT_MODE, decryptKey, IvParameterSpec(iv.decodeBase64()))
            }.doFinal(cipher.decodeBase64())
        )

    private fun ByteArray.encodeBase64() = Base64.encodeToString(this, Base64.DEFAULT)

    private fun String.decodeBase64() = Base64.decode(this, Base64.DEFAULT)
}