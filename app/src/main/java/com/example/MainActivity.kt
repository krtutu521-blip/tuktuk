package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

sealed interface AppScreen {
    object Splash : AppScreen
    object RoleSelect : AppScreen
    data class Auth(val role: String) : AppScreen
    object ParentDashboard : AppScreen
    object MapScreen : AppScreen
    object ChildDashboard : AppScreen
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        val app = application as ParentControlApplication
        MainViewModelFactory(app, app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val currentUser by mainViewModel.currentUser.collectAsState()
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

                // Synchronize active security session gates reactively
                LaunchedEffect(currentUser) {
                    val user = currentUser
                    if (user == null) {
                        // If no session remains active, force gateway select (except in Splash, Auth, and RoleSelect)
                        if (currentScreen !in listOf(AppScreen.Splash, AppScreen.RoleSelect) && currentScreen !is AppScreen.Auth) {
                            currentScreen = AppScreen.RoleSelect
                        }
                    } else {
                        // If authenticated, skip auth screens straight to appropriate dashboard node
                        if (currentScreen in listOf(AppScreen.Splash, AppScreen.RoleSelect) || currentScreen is AppScreen.Auth) {
                            currentScreen = if (user.role == "parent") {
                                AppScreen.ParentDashboard
                            } else {
                                AppScreen.ChildDashboard
                            }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentScreen, label = "navigation") { screen ->
                            when (screen) {
                                is AppScreen.Splash -> {
                                    SplashScreen(onTimeout = {
                                        val user = currentUser
                                        currentScreen = if (user != null) {
                                            if (user.role == "parent") AppScreen.ParentDashboard else AppScreen.ChildDashboard
                                        } else {
                                            AppScreen.RoleSelect
                                        }
                                    })
                                }
                                is AppScreen.RoleSelect -> {
                                    RoleSelectionScreen(onRoleSelected = { selectedRole ->
                                        currentScreen = AppScreen.Auth(selectedRole)
                                    })
                                }
                                is AppScreen.Auth -> {
                                    AuthScreen(
                                        initRole = screen.role,
                                        viewModel = mainViewModel,
                                        onBack = { currentScreen = AppScreen.RoleSelect }
                                    )
                                }
                                is AppScreen.ParentDashboard -> {
                                    ParentDashboardScreen(
                                        viewModel = mainViewModel,
                                        onOpenMap = { currentScreen = AppScreen.MapScreen }
                                    )
                                }
                                is AppScreen.MapScreen -> {
                                    LiveMapScreen(
                                        viewModel = mainViewModel,
                                        onBack = { currentScreen = AppScreen.ParentDashboard }
                                    )
                                }
                                is AppScreen.ChildDashboard -> {
                                    ChildDashboardScreen(
                                        viewModel = mainViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
