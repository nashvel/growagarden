package com.glowagarden.stocknotifier.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.provider.Settings
import com.glowagarden.stocknotifier.R // Assuming nacht.jpg is in res/drawable

@Composable
fun SettingsScreen(stockViewModel: StockViewModel) { // Added stockViewModel parameter
    var preventClosing by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    val selectedSound by stockViewModel.selectedNotificationSound.collectAsState()
    val mediaPlayer = remember { MediaPlayer() }
    val context = LocalContext.current

    val soundOptions = mapOf(
        "default.mp3" to "Default",
        "Raining Tacos.mp3" to "Raining Tacos",
        "cat-laugh-meme-1.mp3" to "Cat Laugh Meme",
        "notification2.mp3" to "Notification Sound 2",
        "notification3.mp3" to "Notification Sound 3",
        "notification4.mp3" to "Notification Sound 4"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.icon), // Make sure icon.png is in res/drawable
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxWidth(0.2f) // Further reduced size to 20% of screen width
                .aspectRatio(1f)    // Maintain square aspect ratio
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Fit // Change to Fit to prevent cropping
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                SettingsItemRow(
                    title = "Permissions",
                    onClick = {
                        // Navigate to app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItemRow(
                    title = "Notification Sound",
                    subtitle = soundOptions[selectedSound] ?: selectedSound, // Display friendly name or filename
                    onClick = { showSoundDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItemRowSwitch(
                    title = "Prevent Closing (Background Run)",
                    checked = preventClosing,
                    onCheckedChange = { preventClosing = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItemRow(
                    title = "About",
                    onClick = {
                        val assetName = "index.html"
                        val authority = "${context.packageName}.provider"
                        val htmlDir = File(context.cacheDir, "html")
                        if (!htmlDir.exists()) {
                            htmlDir.mkdirs()
                        }
                        val targetFile = File(htmlDir, "about.html")

                        try {
                            // Copy asset to cache directory
                            context.assets.open(assetName).use { inputStream ->
                                FileOutputStream(targetFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }

                            val contentUri: Uri = FileProvider.getUriForFile(context, authority, targetFile)

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "text/html")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: IOException) {
                            android.util.Log.e("SettingsScreen", "Error copying asset file: ", e)
                            // Optionally, show a Toast: "Error preparing About page."
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsScreen", "Error opening About page: ", e)
                            // Optionally, show a Toast: "Could not open About page."
                        }
                    }
                )
            }
        }
    }

    // Release MediaPlayer when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = { Text("Select Notification Sound") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    soundOptions.forEach { (fileName, displayName) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    stockViewModel.saveSelectedNotificationSound(fileName)
                                    try {
                                        mediaPlayer.reset()
                                        val assetManager = context.assets
                                        val descriptor = assetManager.openFd("sounds/$fileName")
                                        mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                                        descriptor.close()
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener { mp -> mp.reset() }
                                    } catch (e: Exception) {
                                        android.util.Log.e("SettingsScreen", "Error playing sound preview: $fileName", e)
                                        // Optionally show a toast or log the error
                                    }
                                    // showSoundDialog = false // Optionally close dialog on selection
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (fileName == selectedSound),
                                onClick = { 
                                    stockViewModel.saveSelectedNotificationSound(fileName)
                                    try {
                                        mediaPlayer.reset()
                                        val assetManager = context.assets
                                        val descriptor = assetManager.openFd("sounds/$fileName")
                                        mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                                        descriptor.close()
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener { mp -> mp.reset() }
                                    } catch (e: Exception) {
                                        android.util.Log.e("SettingsScreen", "Error playing sound preview: $fileName", e)
                                    }
                                    // showSoundDialog = false // Optionally close dialog on selection
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsItemRow(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = "Navigate")
    }
}

@Composable
fun SettingsItemRowSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Slightly less vertical padding for switch rows
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
