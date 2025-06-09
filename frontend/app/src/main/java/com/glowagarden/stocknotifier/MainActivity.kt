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
import androidx.compose.material.icons.filled.Refresh // Added for refresh icon
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar // Added for TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton // Added for IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector // Added for BottomNavItem icon type
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding // For Scaffold padding
import androidx.compose.foundation.layout.Box // Added for Box composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import com.glowagarden.stocknotifier.worker.StockCheckWorker // For NOTIFICATION_CHANNEL_ID and scheduling
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.content.Context
// import com.glowagarden.stocknotifier.BuildConfig

// Version Config URL
// private const val VERSION_CONFIG_URL = "https://gist.githubusercontent.com/nashvel/7079605785ed92d0324a2b20b92a448c/raw/9abbcbc3f3821d94c8a4c8e574f2902f400fb677/version_config.json"

// Data class for version configuration
/*data class VersionConfig(
    val minRequiredVersionCode: Int,
    val updateUrl: String,
    val deprecationMessage: String,
    val updateButtonText: String,
    val dialogTitle: String
)*/

// Retrofit service interface
/*interface VersionCheckService {
    @GET
    suspend fun getVersionConfig(@Url url: String): VersionConfig
}*/

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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel() // Called once
        scheduleStockCheckWorker()
        setContent {
            // Version Check State - Temporarily Commented Out
            /*
            var showUpdateDialog by remember { mutableStateOf(false) }
            var versionConfigData by remember { mutableStateOf<VersionConfig?>(null) }
            val coroutineScope = rememberCoroutineScope()
            val contextForIntent = LocalContext.current // For opening URL

            // Retrofit instance for version check
            val retrofit = remember {
                Retrofit.Builder()
                    .baseUrl("https://gist.githubusercontent.com/") // Base URL, actual path in @GET or @Url
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            val versionCheckService = remember {
                retrofit.create(VersionCheckService::class.java)
            }

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    try {
                        // val config = versionCheckService.getVersionConfig(VERSION_CONFIG_URL) // Assuming VersionCheckService and VersionConfig are commented out
                        // val currentVersionCode = com.glowagarden.stocknotifier.BuildConfig.VERSION_CODE // Assumes BuildConfig is available
                        // if (currentVersionCode < config.minRequiredVersionCode) { // config would be null here
                        //     versionConfigData = config
                        //     showUpdateDialog = true
                        // }
                        // Log.d("VersionCheck", "Current: $currentVersionCode, MinRequired: ${config.minRequiredVersionCode}")
                    } catch (e: Exception) {
                        Log.e("VersionCheck", "Error fetching version config", e)
                        // Handle error, e.g., allow app to continue or show a less intrusive error
                    }
                }
            }
            */
                var useDarkTheme by remember { mutableStateOf(false) } // Will be initialized below
            val coroutineScope = rememberCoroutineScope() // Moved here for wider access
            useDarkTheme = isSystemInDarkTheme() // Initialize here, inside composable scope

            GlowAGardenStockNotifierTheme(darkTheme = useDarkTheme) {
                // ViewModel and screen state hoisted to be available for all components within the theme
                val userPreferencesRepository = UserPreferencesRepository(applicationContext)
                val stockViewModelFactory = StockViewModelFactory(userPreferencesRepository)
                val stockViewModel: StockViewModel = viewModel(factory = stockViewModelFactory)
                
                // Observe initial setup completion status
                val initialSetupDone by userPreferencesRepository.initialSetupComplete.collectAsState(initial = false) // Default to false until loaded
                var currentAppFlowState by remember(initialSetupDone) { 
                    mutableStateOf(if (initialSetupDone) AppFlowState.MainApp else AppFlowState.InitialPreferences)
                }
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
                    // if (showUpdateDialog && versionConfigData != null) { // Temporarily commented out
                    //     AlertDialog(
                    //         onDismissRequest = { /* Non-dismissible */ },
                    //         title = { Text(versionConfigData!!.dialogTitle) },
                    //         text = { Text(versionConfigData!!.deprecationMessage) },
                    //         confirmButton = {
                    //             Button(
                    //                 onClick = {
                    //                     val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionConfigData!!.updateUrl))
                    //                     try {
                    //                         contextForIntent.startActivity(intent)
                    //                     } catch (e: Exception) {
                    //                         Log.e("VersionCheck", "Error opening update URL", e)
                    //                         // Optionally show a toast if URL can't be opened
                    //                     }
                    //                 }
                    //             ) {
                    //                 Text(versionConfigData!!.updateButtonText)
                    //             }
                    //         },
                    //         dismissButton = null // No dismiss button to make it mandatory
                    //     )
                    // } else { // Temporarily making this the main path
                    if (currentAppFlowState == AppFlowState.InitialPreferences) {
                        PreferenceScreen(
                            viewModel = stockViewModel,
                            onNavigateToStockScreen = {
                                coroutineScope.launch {
                                    userPreferencesRepository.markInitialSetupComplete()
                                }
                                currentAppFlowState = AppFlowState.MainApp
                                selectedBottomNavItem = BottomNavItem.Home // Default to Home after prefs
                            }
                        )
                    } else {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = selectedBottomNavItem.title) }, 
                                    actions = {
                                        if (selectedBottomNavItem == BottomNavItem.Home) {
                                            TextButton(onClick = { stockViewModel.fetchStockData() }) {
                                                Text("Refresh", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            },
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
                                    BottomNavItem.Settings -> SettingsScreen(stockViewModel = stockViewModel)
                                }
                            }
                        } // Closes Scaffold
                    } // Closes else for currentAppFlowState (if (currentAppFlowState == AppFlowState.InitialPreferences))
                } // Closes Surface
            } // Closes GlowAGardenStockNotifierTheme
        } // Closes setContent
    } // Closes onCreate
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name) 
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            // Use the StockCheckWorker.NOTIFICATION_CHANNEL_ID
            val channel = NotificationChannel(com.glowagarden.stocknotifier.worker.StockCheckWorker.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("MainActivity", "Notification channel created: ${com.glowagarden.stocknotifier.worker.StockCheckWorker.NOTIFICATION_CHANNEL_ID}")
        } else {
            Log.d("MainActivity", "Notification channel not needed for this API level.")
        }
    }

    private fun scheduleStockCheckWorker() {
        val workRequest = PeriodicWorkRequestBuilder<StockCheckWorker>(
            1, TimeUnit.HOURS // Repeat interval: 1 hour
            // You can add constraints here, e.g., network connected, battery not low, etc.
            // .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "StockCheckWorkerPeriodic", // A unique name for this work
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Or REPLACE if you want to update the worker
            workRequest
        )
        Log.d("MainActivity", "StockCheckWorker scheduled to run periodically.")
    }
} // Closes MainActivity class
