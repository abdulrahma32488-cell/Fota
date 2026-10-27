package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.FlotaTheme
import com.example.ui.viewmodel.FlotaViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlotaTheme {
                val viewModel: FlotaViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val statusMessage by viewModel.statusMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearStatusMessage()
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_crossfade",
                        modifier = Modifier.padding(innerPadding)
                    ) { screen ->
                        when (screen) {
                            Screen.HOME -> HomeScreen(viewModel = viewModel)
                            Screen.CONVERSATION -> ConversationScreen(viewModel = viewModel)
                            Screen.WORKSPACE -> ProjectWorkspaceScreen(viewModel = viewModel)
                            Screen.PROJECTS -> ProjectsListScreen(viewModel = viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            Screen.SCIENCE_PORTAL -> SciencePortalScreen(viewModel = viewModel)
                            Screen.BLUEPRINT -> BlueprintScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
