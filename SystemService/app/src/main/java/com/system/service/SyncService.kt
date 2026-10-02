package com.system.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

class SyncService : Service() {

    private var running = true

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        thread {
            while (running) {
                try {
                    CloudAPI.send("🟢 Sync: ${System.currentTimeMillis()}")
                    Thread.sleep(10000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "sync_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "System Sync",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("System Service")
            .setContentText("Đang đồng bộ...")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build()
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}