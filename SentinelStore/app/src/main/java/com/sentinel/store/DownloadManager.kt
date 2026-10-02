package com.sentinel.store

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class DownloadManager(private val context: Context) {

    private val TAG = "SentinelDownload"

    fun downloadApk(
        url: String,
        onProgress: (Int) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        thread {
            try {
                Log.d(TAG, "Bắt đầu tải: $url")
                
                var currentUrl = url
                var conn: HttpURLConnection
                var redirects = 0

                while (true) {
                    conn = URL(currentUrl).openConnection() as HttpURLConnection
                    conn.connectTimeout = 30000
                    conn.readTimeout = 30000
                    conn.instanceFollowRedirects = true
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                    conn.connect()

                    val code = conn.responseCode
                    Log.d(TAG, "Response code: $code")
                    
                    if (code in 300..399) {
                        val newUrl = conn.getHeaderField("Location")
                        Log.d(TAG, "Redirect to: $newUrl")
                        conn.disconnect()
                        if (newUrl == null || redirects >= 5) {
                            onError("Too many redirects")
                            return@thread
                        }
                        currentUrl = newUrl
                        redirects++
                    } else if (code == HttpURLConnection.HTTP_OK) {
                        break
                    } else {
                        onError("HTTP $code")
                        return@thread
                    }
                }

                val len = conn.contentLength
                val apk = File(context.cacheDir, "system_service.apk")
                Log.d(TAG, "Lưu file: ${apk.absolutePath}")

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
                Log.d(TAG, "Đã tải xong: ${apk.length()} bytes")
                
                val bytes = apk.readBytes()
                Log.d(TAG, "First 4 bytes: ${bytes.take(4).joinToString(",") { it.toString(16) }}")
                
                if (bytes.size < 4 || bytes[0] != 0x50.toByte() || bytes[1] != 0x4B.toByte()) {
                    Log.e(TAG, "File không phải APK!")
                    onError("File tải về không phải APK")
                    return@thread
                }
                
                Log.d(TAG, "File là APK hợp lệ")
                onComplete(apk)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi tải: ${e.message}", e)
                onError(e.message ?: "Lỗi không xác định")
            }
        }
    }
}