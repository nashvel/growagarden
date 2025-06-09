package com.glowagarden.stocknotifier.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color // Specific import for Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import com.glowagarden.stocknotifier.StockItem
import com.glowagarden.stocknotifier.StockResponse
import com.glowagarden.stocknotifier.ui.theme.* // Wildcard for theme colors like TierCommon, TierUncommon etc.
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

private const val SEED_REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
private const val GEAR_REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
private const val PET_REFRESH_INTERVAL_MS = 30 * 60 * 1000L  // 30 minutes (for Pets/Eggs)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(stockViewModel: StockViewModel = viewModel()) {
    val stockResponse by stockViewModel.stockData.collectAsState()
    val isLoading by stockViewModel.isLoading.collectAsState()
    val errorMessage by stockViewModel.errorMessage.collectAsState()

    val currentTime = remember { mutableStateOf("") }
    val timeFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = timeFormat.format(Date())
            delay(1000L) // Update every second
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Grow a Garden Stock") },
                actions = {
                    IconButton(onClick = { stockViewModel.fetchStockData() }, enabled = !isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading && stockResponse == null) { // Show full screen loader only on initial load
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Section
                    item {
                        StatusInfoSection(stockResponse, isLoading, currentTime.value)
                    }

                    if (errorMessage != null && stockResponse == null) {
                        item {
                            ErrorCard(message = errorMessage ?: "An unknown error occurred.")
                        }
                    } else {
                         stockResponse?.let { response ->
                            if (response.error_message != null && response.source == "cache (stale)") {
                                item {
                                    WarningCard(message = response.error_message)
                                }
                            }
                            item { 
                                StockCategoryCard(
                                    title = "Seeds", 
                                    icon = Icons.Filled.Grass, 
                                    items = response.seeds, 
                                    isLoading = isLoading && response.seeds.isEmpty(), 
                                    refreshIntervalMillis = SEED_REFRESH_INTERVAL_MS,
                                    onTimerFinished = { stockViewModel.fetchStockData() }
                                )
                            }
                            item { 
                                StockCategoryCard(
                                    title = "Gear", 
                                    icon = Icons.Filled.SettingsApplications, 
                                    items = response.gear, 
                                    isLoading = isLoading && response.gear.isEmpty(), 
                                    refreshIntervalMillis = GEAR_REFRESH_INTERVAL_MS,
                                    onTimerFinished = { stockViewModel.fetchStockData() }
                                )
                            }
                            item { 
                                StockCategoryCard(
                                    title = "Pets", 
                                    icon = Icons.Filled.Pets, 
                                    items = response.eggs, 
                                    isLoading = isLoading && response.eggs.isEmpty(), 
                                    refreshIntervalMillis = PET_REFRESH_INTERVAL_MS,
                                    onTimerFinished = { stockViewModel.fetchStockData() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusInfoSection(stockResponse: StockResponse?, isLoading: Boolean, currentTime: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        val sdf = SimpleDateFormat("hh:mm:ss a, MMM dd", Locale.getDefault())
        val lastUpdatedText = if (stockResponse?.fetched_at_timestamp != null && stockResponse.fetched_at_timestamp != 0L) {
             "Last Updated: ${sdf.format(Date(stockResponse.fetched_at_timestamp))}"
        } else if (isLoading) {
            "Last Updated: Checking..."
        }
        else {
            "Last Updated: Not available"
        }
        Text(lastUpdatedText, style = MaterialTheme.typography.bodySmall)
        Text("Current Time: $currentTime", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}


@Composable
fun StockCategoryCard(
    title: String, 
    icon: ImageVector, 
    items: List<StockItem>, 
    isLoading: Boolean,
    refreshIntervalMillis: Long,
    onTimerFinished: () -> Unit // New callback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Pass refreshIntervalMillis and items.size as keys to re-calculate if they change significantly
            // items.size is a proxy for data changing, which might influence a desire to re-pin the timer
            val calculatedTargetTimeMillis = remember(refreshIntervalMillis, items.hashCode()) {
                val cal = Calendar.getInstance() // Current time
                val intervalInMinutes = (refreshIntervalMillis / (60 * 1000)).toInt()
                
                val currentTotalMinutesInDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                val intervalsPassedToday = currentTotalMinutesInDay / intervalInMinutes
                val nextIntervalMinuteInDay = (intervalsPassedToday + 1) * intervalInMinutes
                
                val targetCalendar = Calendar.getInstance()
                targetCalendar.set(Calendar.HOUR_OF_DAY, nextIntervalMinuteInDay / 60)
                targetCalendar.set(Calendar.MINUTE, nextIntervalMinuteInDay % 60)
                targetCalendar.set(Calendar.SECOND, 0)
                targetCalendar.set(Calendar.MILLISECOND, 0)

                // If current time is exactly on an interval (e.g. 10:05:00 for a 5-min interval)
                // and the calculated target is also 10:05:00, the diff will be 0.
                // In this case, the target should be the *next* interval (10:10:00).
                if (targetCalendar.timeInMillis <= cal.timeInMillis) {
                    targetCalendar.add(Calendar.MINUTE, intervalInMinutes)
                }
                targetCalendar.timeInMillis
            }
            CountdownTimerText(
                targetTimeMillis = calculatedTargetTimeMillis, 
                categoryName = title,
                onTimerFinished = onTimerFinished // Pass the callback
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Text("No ${title.lowercase()} currently in stock.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
            } else {
                items.forEach { item ->
                    StockItemRow(item)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// Helper data class for tier styling
data class TierStyle(val prefix: String, val color: Color, val isPrismatic: Boolean = false)

// Helper function to get style based on tier
@Composable
fun getTierStyle(tier: String?): TierStyle {
    val defaultColor = MaterialTheme.colorScheme.onSurface // Default color if tier is null or unknown
    return when (tier?.lowercase(Locale.getDefault())) {
        "common" -> TierStyle("", TierCommon)
        "uncommon" -> TierStyle("", TierUncommon)
        "rare" -> TierStyle("", TierRare)
        "legendary" -> TierStyle("L - ", TierLegendary)
        "mythical" -> TierStyle("M - ", TierMythical)
        "divine" -> TierStyle("D - ", TierDivine)
        "prismatic" -> TierStyle("P - ", Color.Transparent, isPrismatic = true) // Color handled by animation
        else -> TierStyle("", defaultColor)
    }
}

@Composable
fun StockItemRow(item: StockItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tierStyle = getTierStyle(item.tier)
        val itemName = "${tierStyle.prefix}${item.name}"

        if (tierStyle.isPrismatic) {
            var currentColorIndex by remember { mutableStateOf(0) }
            val animatedColor by animateColorAsState(
                targetValue = TierPrismaticColors[currentColorIndex],
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                label = "PrismaticColorAnimation"
            )
            LaunchedEffect(Unit) { // Use Unit to run once and loop indefinitely
                while (true) {
                    delay(1000) // Duration of each color in the cycle
                    currentColorIndex = (currentColorIndex + 1) % TierPrismaticColors.size
                }
            }
            Text(
                itemName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = animatedColor
            )
        } else {
            Text(
                itemName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = tierStyle.color
            )
        }
        Text("Stock: ${item.stock}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CountdownTimerText(targetTimeMillis: Long?, categoryName: String, onTimerFinished: () -> Unit) {
    var timeLeftFormatted by remember { mutableStateOf("Timer: Initializing...") }
    val TAG_TIMER = "CountdownTimer"

    LaunchedEffect(key1 = targetTimeMillis, key2 = categoryName) {
        Log.d(TAG_TIMER, "[$categoryName] TargetTimeMillis: $targetTimeMillis")
        if (targetTimeMillis == null || targetTimeMillis == 0L) {
            timeLeftFormatted = "Timer: Waiting for data..."
            Log.d(TAG_TIMER, "[$categoryName] Set to: Waiting for data")
            return@LaunchedEffect
        }

        while (true) {
            val currentTime = System.currentTimeMillis()
            val diff = targetTimeMillis - currentTime

            if (diff <= 0) {
                timeLeftFormatted = "Timer: Refreshing..." // Changed message
                Log.d("CountdownTimer", "Timer finished for $categoryName. Calling onTimerFinished.")
                onTimerFinished() // Call the callback
                break // Stop this LaunchedEffect's loop
            }

            val minutes = (diff / (1000 * 60)) % 60
            val seconds = (diff / 1000) % 60

            timeLeftFormatted = String.format(Locale.getDefault(), "Timer: %02d min %02d sec", minutes, seconds)
            // Log.d(TAG_TIMER, "[$categoryName] Set to: $timeLeftFormatted") // Can be noisy
            delay(1000) // Update every second
        }
    }
    Text(timeLeftFormatted, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun WarningCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
         elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
