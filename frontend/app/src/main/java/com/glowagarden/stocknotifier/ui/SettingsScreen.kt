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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun SettingsScreen() {
    var preventClosing by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                .fillMaxWidth(0.3f) // Reduced size from 0.5f
                .aspectRatio(1f)    // Adjust aspect ratio as needed
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
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
}

@Composable
fun SettingsItemRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
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
