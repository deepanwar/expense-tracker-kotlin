package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ExpenseTracker)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                // ponytail: splash off; restore SplashScreen + delay when wanted
                MainScreen()
            }
        }
    }
}
