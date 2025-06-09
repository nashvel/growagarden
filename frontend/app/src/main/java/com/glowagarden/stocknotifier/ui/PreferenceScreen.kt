package com.glowagarden.stocknotifier.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import java.io.IOException
import android.util.Log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Added for mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.compose.material.icons.Icons // Removed duplicate
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link // For FB icon placeholder
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalUriHandler // To open URL
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.shape.CircleShape // For circular image
import com.glowagarden.stocknotifier.R // For R.drawable.icon
import com.glowagarden.stocknotifier.model.SelectableItem
// import androidx.compose.ui.graphics.Color // Ensure Color is imported - Removed duplicate

// Helper function to generate image filename from item name
private fun getItemImageFileName(itemName: String): String {
    return itemName.replace(" ", "").lowercase() + ".png"
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null, // Optional icon parameter
    cornerRadius: Dp = 16.dp, 
    shadowElevation: Dp = 8.dp, // Refined Neumorphic elevation
    paddingValues: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    buttonBackgroundColor: Color = MaterialTheme.colorScheme.background, // New parameter
    contentColor: Color = MaterialTheme.colorScheme.onSurface // New parameter
) {
    val surfaceColor = buttonBackgroundColor
    val isDarkTheme = isSystemInDarkTheme()

    val lightShadowColor: Color
    val darkShadowColor: Color

    if (isDarkTheme) {
        lightShadowColor = Color.White.copy(alpha = 0.12f) // Dark theme: more visible light shadow
        darkShadowColor = Color.Black.copy(alpha = 0.35f)  // Dark theme: strong dark shadow
    } else {
        lightShadowColor = Color.White.copy(alpha = 0.9f)  // Light theme: strong white highlight
        darkShadowColor = Color(0xFFB0B0B0).copy(alpha = 0.30f) // Light theme: noticeable dark shadow
    }

    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = darkShadowColor,
                    spotColor = lightShadowColor
                )
                .background(surfaceColor, RoundedCornerShape(cornerRadius))
        )
        Row(
            modifier = Modifier.padding(paddingValues),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null, // Decorative
                    tint = contentColor, // Use new contentColor parameter
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor // Use new contentColor parameter
            )
        }
    }
}

@Composable
fun PreferenceScreen(viewModel: StockViewModel, onNavigateToStockScreen: () -> Unit) {
    var cropsExpanded by remember { mutableStateOf(false) }
    var gearExpanded by remember { mutableStateOf(false) }
    var petsExpanded by remember { mutableStateOf(false) }
    val cropPreferences by viewModel.cropPreferences.collectAsState()
    val gearPreferences by viewModel.gearPreferences.collectAsState()
    val petPreferences by viewModel.petPreferences.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Logo and Title Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon), // Assumes icon.png is in res/drawable
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp) // Adjust size as needed
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Grow a Garden Stock Notifier",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp)) // Space between title and profile pic
                Image(
                    painter = painterResource(id = R.drawable.nacht), // Assumes nacht.jpg is in res/drawable
                    contentDescription = "Creator's Profile Picture",
                    modifier = Modifier
                        .size(90.dp) // New size for profile pic
                        .clip(CircleShape) // Make it circular
                )
                Spacer(modifier = Modifier.height(8.dp)) // Space after profile pic, before FB link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val uriHandler = LocalUriHandler.current
                    val facebookUrl = "https://www.facebook.com/github.nashvel"
                    Text(
                        text = "Creator's Link ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Filled.Link, // Placeholder for Facebook Icon
                        contentDescription = "Nacht's Facebook Profile",
                        tint = MaterialTheme.colorScheme.primary, // Make it look like a link
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { uriHandler.openUri(facebookUrl) }
                    )
                    Text(
                        text = ". Use accordingly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(24.dp)) // Original spacer, now after FB link
            }
        }

        // Crops Section
        // Crops Section
        item {
            PreferenceSectionHeader(
                title = "Notify me for these CROP stocks (Legendary above only):",
                icon = Icons.Filled.Spa,
                expanded = cropsExpanded,
                onToggle = {
                    val newState = !cropsExpanded
                    cropsExpanded = newState
                    if (newState) {
                        gearExpanded = false
                        petsExpanded = false
                    }
                }
            )
            Spacer(modifier = Modifier.height(if (cropsExpanded) 8.dp else 0.dp)) // Space below header only when expanded
        }
        if (cropsExpanded) {
            items(cropPreferences, key = { it.name + it.category }) { item ->
                SelectableItemRow(item = item, onToggle = { viewModel.toggleItemSelection(item) })
            }
        }

        // Gear Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceSectionHeader(
                title = "Notify me for these GEAR stocks:",
                icon = Icons.Filled.BuildCircle,
                expanded = gearExpanded,
                onToggle = {
                    val newState = !gearExpanded
                    gearExpanded = newState
                    if (newState) {
                        cropsExpanded = false
                        petsExpanded = false
                    }
                }
            )
            Spacer(modifier = Modifier.height(if (gearExpanded) 8.dp else 0.dp)) // Space below header only when expanded
        }
        if (gearExpanded) {
            items(gearPreferences, key = { it.name + it.category }) { item ->
                SelectableItemRow(item = item, onToggle = { viewModel.toggleItemSelection(item) })
            }
        }

        // Pets Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceSectionHeader(
                title = "Notify me for these PET stocks:",
                icon = Icons.Filled.Pets,
                expanded = petsExpanded,
                onToggle = {
                    val newState = !petsExpanded
                    petsExpanded = newState
                    if (newState) {
                        cropsExpanded = false
                        gearExpanded = false
                    }
                }
            )
            Spacer(modifier = Modifier.height(if (petsExpanded) 8.dp else 0.dp)) // Space below header only when expanded
        }
        if (petsExpanded) {
            items(petPreferences, key = { it.name + it.category }) { item ->
                SelectableItemRow(item = item, onToggle = { viewModel.toggleItemSelection(item) })
            }
        }

        // Disclaimer and Save Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Center the Column's children
            ) {
                // First line: Beta warning
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) // Light Orange Background
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Information",
                        tint = Color(0xFFE65100) // Dark Orange Icon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Beta: Data ~20s delay. Prices and stock may vary slightly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE65100) // Dark Orange Text
                    )
                }

            }
            Spacer(modifier = Modifier.height(16.dp))
            // TEST: Wrap button in a Box to center it if it's not full width
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center 
            ) {
                NeumorphicButton(
                    onClick = {
                        viewModel.saveUserSelections() // Save preferences
                        onNavigateToStockScreen()      // Then navigate
                    },
                    text = "Save and View Stocks",
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier.fillMaxWidth(),
                    buttonBackgroundColor = Color(0xFF4CAF50), // Green background
                    contentColor = Color.White // White text/icon for contrast
                )
            }
            Spacer(modifier = Modifier.height(24.dp)) // Extra space at the bottom
        }
    }
}

@Composable
fun PreferenceSectionHeader(
    title: String,
    icon: ImageVector?, // Added icon parameter
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier, 
    cornerRadius: Dp = 12.dp, 
    shadowElevation: Dp = 8.dp, // Refined Neumorphic elevation
    titleColor: Color = MaterialTheme.colorScheme.primary // New parameter for title color
) {
    val surfaceColor = MaterialTheme.colorScheme.background
    val isDarkTheme = isSystemInDarkTheme()

    val lightShadowColor: Color
    val darkShadowColor: Color

    if (isDarkTheme) {
        lightShadowColor = Color.White.copy(alpha = 0.12f) // Dark theme: more visible light shadow
        darkShadowColor = Color.Black.copy(alpha = 0.35f)  // Dark theme: strong dark shadow
    } else {
        lightShadowColor = Color.White.copy(alpha = 0.9f)  // Light theme: strong white highlight
        darkShadowColor = Color(0xFFB0B0B0).copy(alpha = 0.30f) // Light theme: noticeable dark shadow
    }

    Box( // Outer Box for click handling and overall padding
        modifier = modifier // Use the passed-in modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle) // Clickable area
            .padding(vertical = 8.dp, horizontal = 16.dp) // Padding around the visible element
    ) {
        // Shadow and background element
        Spacer(
            modifier = Modifier
                .matchParentSize() // Matches the size of the clickable Box's content area
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = darkShadowColor,
                    spotColor = lightShadowColor
                )
                .background(surfaceColor, RoundedCornerShape(cornerRadius))
        )
        // Content (Text and Icon) on top of the Spacer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 12.dp), // Inner padding for content within the Spacer
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null, // Decorative
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                color = titleColor // Use new titleColor parameter
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SelectableItemRow(
    item: SelectableItem,
    onToggle: (SelectableItem) -> Unit,
    cornerRadius: Dp = 10.dp,
    shadowElevation: Dp = 6.dp // Refined Neumorphic elevation for items
) {
    val surfaceColor = MaterialTheme.colorScheme.background
    val isDarkTheme = isSystemInDarkTheme()

    val actualAmbientColor: Color
    val actualSpotColor: Color

    if (isDarkTheme) {
        val baseLight = Color.White.copy(alpha = 0.10f) // Dark theme: light part of shadow
        val baseDark = Color.Black.copy(alpha = 0.30f)   // Dark theme: dark part of shadow
        if (item.isSelected) { // Pressed-in effect
            actualSpotColor = baseDark
            actualAmbientColor = baseLight
        } else { // Extruded effect
            actualSpotColor = baseLight
            actualAmbientColor = baseDark
        }
    } else {
        // Light theme: Make shadows more pronounced
        val baseLight = Color.White.copy(alpha = 0.85f) // Light theme: strong white highlight
        val baseDark = Color(0xFFB8B8B8).copy(alpha = 0.25f) // Light theme: noticeable dark shadow
        if (item.isSelected) { // Pressed-in effect
            actualSpotColor = baseDark
            actualAmbientColor = baseLight
        } else { // Extruded effect
            actualSpotColor = baseLight
            actualAmbientColor = baseDark
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Spacing between items
            .clickable { onToggle(item) }
    ) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = actualAmbientColor, // Use determined ambient color
                    spotColor = actualSpotColor      // Use determined spot color
                )
                .background(surfaceColor, RoundedCornerShape(cornerRadius))
        )
        val context = LocalContext.current
        val imageBitmap = remember(item.name, context) {
            try {
                val imageFileName = getItemImageFileName(item.name)
                context.assets.open("images/$imageFileName").use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            } catch (e: IOException) {
                Log.e("PreferenceScreen", "Error loading image for ${item.name} (assets/images/${getItemImageFileName(item.name)}): ${e.message}")
                null
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "${item.name} icon",
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Display a placeholder Box to maintain layout consistency if image fails to load
                Box(modifier = Modifier.size(32.dp).background(Color.Gray.copy(alpha = 0.1f))) // Optional: placeholder visual
            }
            Spacer(modifier = Modifier.width(12.dp)) // Space between image/placeholder and text

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp)) // Space before the tier text
            Text(
                text = item.tier,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = getColorForTier(item.tier) // Apply color here
            )
            Spacer(modifier = Modifier.width(8.dp)) // Space after the tier text
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggle(item) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
