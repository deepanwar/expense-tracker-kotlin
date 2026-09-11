package com.example.expensetracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val client: SupabaseClient
) {
    val sessionStatus: StateFlow<SessionStatus> = client.auth.sessionStatus

    suspend fun signInWithGoogleIdToken(idToken: String, nonce: String) {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            this.nonce = nonce
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}
