package com.sentinel.store

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnInstall: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private val MAIN_APP_URL = "https://github.com/Kenbachkhoa1234/SentinelStore/releases/download/v1.0.0/app-debug.apk"
    private val MAIN_APP_PACKAGE = "com.sentinel.agent"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnInstall = findViewById(R.id.btnInstall)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)

        btnInstall.setOnClickListener {
            if (!canInstallPackages()) {
                requestInstallPermission()
                return@setOnClickListener
            }
            startInstallFlow()
        }
    }

    private fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            packageManager.canRequestPackageInstalls()
        } else true
    }

    private fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            Toast.makeText(this, "Cho phép cài đặt từ nguồn này", Toast.LENGTH_LONG).show()
        }
    }

    private fun startInstallFlow() {
        if (isAppInstalled(MAIN_APP_PACKAGE)) {
            Toast.makeText(this, "App đã được cài!", Toast.LENGTH_SHORT).show()
            launchMainApp()
            return
        }

        tvStatus.text = "Đang tải..."
        btnInstall.isEnabled = false
        progressBar.visibility = ProgressBar.VISIBLE

        DownloadManager(this).downloadApk(
            url = MAIN_APP_URL,
            onProgress = { p ->
                runOnUiThread {
                    progressBar.progress = p
                    tvStatus.text = "Đang tải... $p%"
                }
            },
            onComplete = { apkFile ->
                runOnUiThread { tvStatus.text = "Đang cài đặt..." }
                InstallManager(this).installApk(apkFile)
            },
            onError = { err ->
                runOnUiThread {
                    tvStatus.text = "Lỗi: $err"
                    btnInstall.isEnabled = true
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        )
    }

    private fun isAppInstalled(pkg: String): Boolean = try {
        packageManager.getPackageInfo(pkg, 0)
        true
    } catch (e: Exception) { false }

    private fun launchMainApp() {
        packageManager.getLaunchIntentForPackage(MAIN_APP_PACKAGE)?.let {
            startActivity(it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (canInstallPackages()) btnInstall.isEnabled = true
    }
}