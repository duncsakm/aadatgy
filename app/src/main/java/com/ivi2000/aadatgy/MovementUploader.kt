package com.ivi2000.aadatgy

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object MovementUploader {

    fun upload(
        context: Context,
        type: MovementType,
        file: File,
        ip: String,
        port: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        Thread {
            var conn: HttpURLConnection? = null
            try {
                val path = when (type) {
                    MovementType.RENDELES -> "/vrhad"
                    MovementType.LELTAR -> "/leltar"
                }

                val url = URL("http://$ip:$port$path")
                val bodyBytes = file.readBytes()

                conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                    setRequestProperty("Content-Length", bodyBytes.size.toString())
                }

                conn.outputStream.use { out ->
                    out.write(bodyBytes)
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val msg = context.getString(R.string.err_http_code, conn.responseCode)
                    callback(false, msg)
                }

            } catch (e: Exception) {
                val msg = context.getString(R.string.err_unknown)
                callback(false, e.message ?: msg)
            } finally {
                conn?.disconnect()
            }
        }.start()
    }
}
