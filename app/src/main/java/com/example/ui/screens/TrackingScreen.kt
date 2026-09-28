package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.ui.viewmodel.MainViewModel

@Composable
fun TrackingScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onRideFinished: (Long) -> Unit
) {
    TrackingScreenUI(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onRideFinished = onRideFinished
    )
}
