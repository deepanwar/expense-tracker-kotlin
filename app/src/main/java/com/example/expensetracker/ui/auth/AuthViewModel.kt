package com.example.expensetracker.ui.auth

import android.app.Activity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.data.auth.GoogleSignInHelper
import com.example.expensetracker.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isReady: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isSigningIn: Boolean = false,
    val email: String? = null,
    val errorRes: Int? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val googleSignInHelper: GoogleSignInHelper = GoogleSignInHelper()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                _uiState.update { current -> current.withSessionStatus(status) }
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        if (_uiState.value.isSigningIn) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, errorRes = null) }
            try {
                val token = googleSignInHelper.fetchIdToken(activity)
                authRepository.signInWithGoogleIdToken(token.idToken, token.nonce)
            } catch (_: GetCredentialCancellationException) {
                _uiState.update { it.copy(isSigningIn = false) }
            } catch (_: NoCredentialException) {
                _uiState.update {
                    it.copy(isSigningIn = false, errorRes = R.string.sign_in_no_google_account)
                }
            } catch (_: GoogleIdTokenParsingException) {
                _uiState.update { it.copy(isSigningIn = false, errorRes = R.string.sign_in_error) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSigningIn = false, errorRes = R.string.sign_in_error) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (_: Exception) {
                _uiState.update { it.copy(errorRes = R.string.sign_out_error) }
            }
        }
    }
}

private fun AuthUiState.withSessionStatus(status: SessionStatus): AuthUiState {
    return when (status) {
        is SessionStatus.Initializing -> copy(isReady = false)
        is SessionStatus.RefreshFailure -> copy(
            isReady = true,
            isLoggedIn = false,
            isSigningIn = false,
            email = null
        )
        is SessionStatus.NotAuthenticated -> copy(
            isReady = true,
            isLoggedIn = false,
            isSigningIn = false,
            email = null
        )
        is SessionStatus.Authenticated -> copy(
            isReady = true,
            isLoggedIn = true,
            isSigningIn = false,
            email = status.session.user?.email,
            errorRes = null
        )
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
