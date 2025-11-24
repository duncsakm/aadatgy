package com.ivi2000.aadatgy

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class CikktorzsUpdater(private val context: Context) {

    private val client = OkHttpClient()

    fun downloadWplrad(serverIp: String, port: String, onDone: (Boolean, String?) -> Unit) {
        val url = "http://$serverIp:$port/wplrad"
        val request = Request.Builder().url(url).build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        onDone(false, context.getString(R.string.err_http, response.code))
                        return@use
                    }

                    val bodyString = response.body?.string().orEmpty()
                    if (bodyString.isBlank()) {
                        onDone(false, context.getString(R.string.err_empty_wplrad))
                        return@use
                    }

                    File(context.filesDir, "wplrad.txt")
                        .writeText(bodyString, Charsets.UTF_8)

                    onDone(true, null)
                }
            } catch (e: Exception) {
                onDone(false, context.getString(R.string.err_download_exception, e.message ?: ""))
            }
        }.start()
    }
}
