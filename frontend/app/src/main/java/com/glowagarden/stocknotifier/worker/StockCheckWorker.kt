package com.glowagarden.stocknotifier.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.glowagarden.stocknotifier.UserPreferencesRepository
import com.glowagarden.stocknotifier.network.ApiService
import com.glowagarden.stocknotifier.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.glowagarden.stocknotifier.MainActivity
import com.glowagarden.stocknotifier.R
import com.glowagarden.stocknotifier.StockItem

class StockCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Companion object for TAG and other constants
    companion object {
        const val TAG = "StockCheckWorker"
        const val NOTIFICATION_CHANNEL_ID = "stock_notifier_channel"
        const val NOTIFICATION_ID = 1 // Unique ID for this notification type
    }

    // Services and Repositories will be needed.
    // These would ideally be injected (e.g., using Hilt) in a larger app,
    // but for simplicity, we can instantiate them directly or pass their dependencies.
    private val apiService: ApiService by lazy {
        RetrofitInstance.api // Assumes RetrofitInstance is set up correctly
    }
    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "StockCheckWorker started.")

        return withContext(Dispatchers.IO) {
            try {
                // 1. Fetch latest stock data from the API
                Log.d(TAG, "Fetching stock data...")
                val stockResponse = apiService.getStockData()
                Log.d(TAG, "Stock data received: ${stockResponse.source}")

                // 2. Get user's preferred items from DataStore
                Log.d(TAG, "Fetching user preferences...")
                val preferredItemNames = userPreferencesRepository.loadSelectedItems.first()
                val selectedSoundUri = userPreferencesRepository.selectedNotificationSound.first()
                Log.d(TAG, "Preferred items: $preferredItemNames, Sound: $selectedSoundUri")

                // 3. Compare stock data with preferences
                val itemsToNotify = mutableListOf<com.glowagarden.stocknotifier.StockItem>()

                val allStockItems = (stockResponse.seeds ?: emptyList()) + 
                                      (stockResponse.gear ?: emptyList()) + 
                                      (stockResponse.eggs ?: emptyList())

                for (item in allStockItems) {
                    if (item.stock > 0 && preferredItemNames.contains(item.name)) {
                        itemsToNotify.add(item)
                    }
                }

                if (itemsToNotify.isNotEmpty()) {
                    Log.d(TAG, "Items to notify about: ${itemsToNotify.joinToString { it.name }}")
                    sendNotification(itemsToNotify, selectedSoundUri)
                } else {
                    Log.d(TAG, "No preferred items currently in stock to notify about.")
                }

                // 4. If any such items are found, build and show a notification
                // Use the selectedSound for the notification.

                Log.d(TAG, "StockCheckWorker completed successfully.")
                Result.success()

            } catch (e: Exception) {
                Log.e(TAG, "Error in StockCheckWorker: ${e.message}", e)
                Result.failure() // Or Result.retry() if appropriate
            }
        }
    }

    private fun sendNotification(items: List<StockItem>, soundFileName: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationTitle = "Stock Alert!"
        val notificationText = if (items.size == 1) {
            "${items.first().name} is on stock hurry up! before it refresh"
        } else {
            "Multiple items are back in stock: ${items.take(3).joinToString { it.name }}${if (items.size > 3) " and more" else ""}!"
        }

        val soundNameWithoutExtension = soundFileName.substringBeforeLast('.')
        val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/raw/$soundNameWithoutExtension")

        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon) // Use app's main icon
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)

        // Before Android 8.0 (API 26), channel ID is ignored.
        // For Android 8.0+, channel must be created before posting notifications.
        // We will create the channel in MainActivity or Application class.

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            try {
                notify(NOTIFICATION_ID, builder.build())
                Log.d(TAG, "Notification sent for items: ${items.joinToString { it.name }}")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException when trying to send notification. Do we have POST_NOTIFICATIONS permission?", e)
                // This might happen on Android 13+ if permission is not granted.
            }
        }
    }
}