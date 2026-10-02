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
                    if (code in 300..399) {
                        val newUrl = conn.getHeaderField("Location")
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
                
                // Kiểm tra file có phải APK không (magic bytes: PK)
                val bytes = apk.readBytes()
                if (bytes.size < 4 || bytes[0] != 0x50.toByte() || bytes[1] != 0x4B.toByte()) {
                    onError("File tải về không phải APK")
                    return@thread
                }
                
                onComplete(apk)
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi không xác định")
            }
        }
    }
}