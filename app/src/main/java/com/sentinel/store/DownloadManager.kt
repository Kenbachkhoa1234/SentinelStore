package com.sentinel.store

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class DownloadManager(private val context: Context) {

    fun downloadApk(
        url: String,
        onProgress: (Int) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                conn.connect()

                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    onError("HTTP ${conn.responseCode}")
                    return@thread
                }

                val len = conn.contentLength
                val apk = File(context.cacheDir, "sentinel_agent.apk")

                conn.inputStream.use { input ->
                    FileOutputStream(apk).use { output ->
                        val buf = ByteArray(8192)
                        var read: Int
                        var total = 0L
                        while (input.read(buf).also { read = it } != -1) {
                            output.write(buf, 0, read)
                            total += read
                            if (len > 0) onProgress((total * 100 / len).toInt())
                        }
                    }
                }

                conn.disconnect()
                onComplete(apk)
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi không xác định")
            }
        }
    }
}