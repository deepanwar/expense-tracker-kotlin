package com.example.expensetracker.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light",
    showBackground = true
)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
annotation class AppPreview