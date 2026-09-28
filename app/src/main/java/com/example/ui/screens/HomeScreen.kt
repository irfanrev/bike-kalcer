package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTracking: () -> Unit
) {
    HomeScreenUI(viewModel = viewModel, onNavigateToTracking = onNavigateToTracking)
}
