package com.glowagarden.stocknotifier.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.glowagarden.stocknotifier.model.SelectableItem
import androidx.compose.foundation.isSystemInDarkTheme
// import androidx.compose.material3.MaterialTheme // Already imported above or via ColorUtils
import androidx.compose.ui.graphics.Color
// import androidx.compose.material3.pullrefresh.PullRefreshIndicator // Keep commented
// import androidx.compose.material3.pullrefresh.pullRefresh // Keep commented
// import androidx.compose.material3.pullrefresh.rememberPullRefreshState // Keep commented
// import androidx.compose.material3.ExperimentalMaterial3Api // Keep commented

// @OptIn(ExperimentalMaterial3Api::class) // Keep commented
@Composable
fun NotificationListScreen(
    stockViewModel: StockViewModel,
    onManageSelectionsClick: () -> Unit
) {
    val cropPreferences by stockViewModel.cropPreferences.collectAsState()
    val gearPreferences by stockViewModel.gearPreferences.collectAsState()
    val petPreferences by stockViewModel.petPreferences.collectAsState()

    val selectedCrops = cropPreferences.filter { it.isSelected }
    val selectedGear = gearPreferences.filter { it.isSelected }
    val selectedPets = petPreferences.filter { it.isSelected }
    val totalSelectedItems = selectedCrops.size + selectedGear.size + selectedPets.size

    // val isRefreshing by stockViewModel.isLoading.collectAsState() // Keep commented
    // val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = { stockViewModel.fetchStockData() }) // Keep commented

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Items You're Tracking",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // .pullRefresh(pullRefreshState) // Keep pull-to-refresh commented
        ) {
            if (totalSelectedItems == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(), // Fill the parent pull-refresh Box
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You are not tracking any items yet.\nClick 'Manage Selections' to choose items.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize() // Fill the parent pull-refresh Box
                ) {
                    // Seeds Section
                    if (selectedCrops.isNotEmpty()) {
                        item {
                            Text("Seeds", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(selectedCrops, key = { "crop-" + it.name + it.category }) { item ->
                            SelectedNotificationItemRow(item = item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) } // Space after section
                    }

                    // Gear Section
                    if (selectedGear.isNotEmpty()) {
                        item {
                            Text("Gear", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(selectedGear, key = { "gear-" + it.name + it.category }) { item ->
                            SelectedNotificationItemRow(item = item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) } // Space after section
                    }

                    // Pets Section
                    if (selectedPets.isNotEmpty()) {
                        item {
                            Text("Pets", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(selectedPets, key = { "pet-" + it.name + it.category }) { item ->
                            SelectedNotificationItemRow(item = item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            // PullRefreshIndicator and its comment are within the larger commented Box
            // TODO: Restore pull-to-refresh
            // PullRefreshIndicator(
            //     refreshing = isRefreshing, // Keep commented
            //     state = pullRefreshState, // Keep commented
            //     modifier = Modifier.align(Alignment.TopCenter)
            // )
            // End of PullRefreshIndicator related code
        } // This closes the Box

        Spacer(modifier = Modifier.height(16.dp)) // Spacer before the button

        NeumorphicButton(
            onClick = onManageSelectionsClick,
            text = "Manage Selections",
            icon = Icons.Filled.Edit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SelectedNotificationItemRow(item: SelectableItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Category: ${item.category}", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = item.tier, 
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = getColorForTier(item.tier)
        )
    }
}