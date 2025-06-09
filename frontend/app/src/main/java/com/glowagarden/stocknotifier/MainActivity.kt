package com.glowagarden.stocknotifier

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector // Added for BottomNavItem icon type
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding // For Scaffold padding
import androidx.compose.foundation.layout.Box // Added for Box composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowagarden.stocknotifier.UserPreferencesRepository // Added import
import com.glowagarden.stocknotifier.ui.StockViewModelFactory // Added import
import com.glowagarden.stocknotifier.ui.PreferenceScreen
import com.glowagarden.stocknotifier.ui.StockScreen
import com.glowagarden.stocknotifier.ui.StockViewModel // Explicit import for StockViewModel
import com.glowagarden.stocknotifier.ui.AppBottomNavigationBar
import com.glowagarden.stocknotifier.ui.NotificationListScreen
import com.glowagarden.stocknotifier.ui.SettingsScreen
import com.glowagarden.stocknotifier.ui.theme.GlowAGardenStockNotifierTheme

// Enum to manage overall app state (initial preferences vs main app)
enum class AppFlowState {
    InitialPreferences,
    MainApp
}

// Enum for Bottom Navigation items
sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Notifications : BottomNavItem("notifications", Icons.Filled.Notifications, "Notifications")
    object Settings : BottomNavItem("settings", Icons.Filled.Settings, "Settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                var useDarkTheme by remember { mutableStateOf(false) } // Will be initialized below
            useDarkTheme = isSystemInDarkTheme() // Initialize here, inside composable scope

            GlowAGardenStockNotifierTheme(darkTheme = useDarkTheme) {
                // ViewModel and screen state hoisted to be available for all components within the theme
                val userPreferencesRepository = UserPreferencesRepository(applicationContext)
                val stockViewModelFactory = StockViewModelFactory(userPreferencesRepository)
                val stockViewModel: StockViewModel = viewModel(factory = stockViewModelFactory)
                var currentAppFlowState by remember { mutableStateOf(AppFlowState.InitialPreferences) }
                var selectedBottomNavItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }

                // Context and permission launcher
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted: Boolean ->
                        if (isGranted) {
                            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Effect for requesting notification permission on launch
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        )) {
                            PackageManager.PERMISSION_GRANTED -> {
                                // Permission is already granted
                            }
                            else -> {
                                // Request permission
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentAppFlowState == AppFlowState.InitialPreferences) {
                        PreferenceScreen(
                            viewModel = stockViewModel,
                            onNavigateToStockScreen = {
                                currentAppFlowState = AppFlowState.MainApp
                                selectedBottomNavItem = BottomNavItem.Home // Default to Home screen after preferences
                            }
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                AppBottomNavigationBar(selectedItem = selectedBottomNavItem) {
                                    selectedBottomNavItem = it
                                }
                            }
                        ) { innerPadding ->
                            // Content of the selected screen
                            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) { // Added fillMaxSize to the Box
                                when (selectedBottomNavItem) {
                                    BottomNavItem.Home -> StockScreen(stockViewModel = stockViewModel)
                                    BottomNavItem.Notifications -> NotificationListScreen(
                                        stockViewModel = stockViewModel,
                                        onManageSelectionsClick = {
                                            currentAppFlowState = AppFlowState.InitialPreferences
                                        }
                                    )
                                    BottomNavItem.Settings -> SettingsScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

