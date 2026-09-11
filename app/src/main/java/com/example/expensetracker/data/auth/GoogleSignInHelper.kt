package com.example.expensetracker.data.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.expensetracker.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

data class GoogleIdTokenResult(
    val idToken: String,
    val nonce: String
)

class GoogleSignInHelper {
    suspend fun fetchIdToken(activity: Activity): GoogleIdTokenResult {
        val rawNonce = UUID.randomUUID().toString()
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(sha256(rawNonce))
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = request
        )
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        return GoogleIdTokenResult(
            idToken = googleIdTokenCredential.idToken,
            nonce = rawNonce
        )
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
