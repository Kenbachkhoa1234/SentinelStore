package com.system.service

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        findViewById<Button>(R.id.btnAdmin).setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(this, AdminHandler::class.java)
            )
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startService(Intent(this, SyncService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val acc = isAccessibilityEnabled()
        val notif = isNotificationEnabled()
        val admin = isAdminEnabled()

        tvStatus.text = """
            Accessibility: ${if (acc) "✅" else "❌"}
            Notification: ${if (notif) "✅" else "❌"}
            Device Admin: ${if (admin) "✅" else "❌"}
        """.trimIndent()
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/.InputMonitor"
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(service)
    }

    private fun isNotificationEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(packageName) == true
    }

    private fun isAdminEnabled(): Boolean {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(ComponentName(this, AdminHandler::class.java))
    }
}