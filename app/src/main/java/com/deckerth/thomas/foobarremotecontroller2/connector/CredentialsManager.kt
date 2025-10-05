package com.deckerth.thomas.foobarremotecontroller2.connector

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.deckerth.thomas.foobarremotecontroller2.getFoobarConnectionsBlocking
import com.deckerth.thomas.foobarremotecontroller2.saveCredentials
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_ALIAS = "foobar_link_key_alias"

class CredentialsManager(val vm: AppViewModel) {

    init {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // Check if the key already exists
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // It's good practice to add this to require user authentication (e.g., fingerprint/PIN)
                // to use the key, which makes it much more secure.
                // .setUserAuthenticationRequired(true)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        CoroutineScope(Dispatchers.IO).launch {
            initConnectionManager()
        }
    }

    private suspend fun initConnectionManager() {
        vm.connectionManager = getFoobarConnectionsBlocking()
    }

    private fun encrypt(password: String): Pair<ByteArray, ByteArray> {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedPassword = cipher.doFinal(password.toByteArray())
        return Pair(cipher.iv, encryptedPassword)
    }

    private fun decrypt(iv: ByteArray, encryptedData: ByteArray): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }

    fun getUser(): String {
        return vm.user
    }

    fun getPassword(): String {
        return vm.password
    }

    fun setNewUserPassword(ipAddress: String, user: String, password: String) {
        vm.user = user
        vm.password = password
        val (iv, encryptedPassword) = encrypt(password)
        saveCredentials(user, encryptedPassword, iv, mainActivity)
        vm.connectionManager?.addConnection(FoobarConnection(ipAddress, user, password, iv))
    }

    fun setCurrentUserPassword(
        ipAddress: String,
        user: String,
        encryptedPassword: ByteArray,
        iv: ByteArray
    ) {
        try {
            if (user.isNotBlank())
                vm.password = decrypt(iv, encryptedPassword)
            else
                vm.password = ""
            vm.user = user
            vm.connectionManager?.addConnection(FoobarConnection(ipAddress, user, vm.password, iv))
        } catch (e: Exception) {
            // Decryption failed! The stored credentials are now invalid.
            // This can happen if the app was reinstalled, data was cleared,
            // or the keystore key changed.
            // We must clear the credentials to prevent a login loop.
            e.printStackTrace() // Log the error for debugging
            vm.user = ""
            vm.password = ""
        }
    }

    fun setIPAddress(ipAddress: String) {
        val connection = vm.connectionManager?.getConnection(ipAddress)
        if (connection != null) {
            vm.user = connection.username
            vm.password = connection.password
        } else {
            vm.user = ""
            vm.password = ""
        }
    }
}