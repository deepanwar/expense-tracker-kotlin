package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.auth.AuthViewModel
import com.example.expensetracker.ui.auth.AuthViewModelFactory
import com.example.expensetracker.ui.auth.LoginScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ExpenseTracker)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                val app = applicationContext as ExpenseTrackerApplication
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(app.authRepository)
                )
                val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
                when {
                    !uiState.isReady -> SplashScreen()
                    !uiState.isLoggedIn -> LoginScreen(viewModel = authViewModel)
                    else -> MainScreen()
                }
            }
        }
    }
}
