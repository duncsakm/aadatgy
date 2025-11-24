package com.ivi2000.aadatgy

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class FoundServer(
    val ip: String,
    val name: String
)

object ServerDiscovery {

    fun scanForServers(
        context: Context,
        port: Int,
        callback: (List<FoundServer>) -> Unit
    ) {
        thread {
            val client = OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.MILLISECONDS)
                .readTimeout(300, TimeUnit.MILLISECONDS)
                .build()

            val basePrefix = getLocalBasePrefix(context) ?: "192.168.0"
            val found = mutableListOf<FoundServer>()

            for (i in 1..254) {
                val ip = "$basePrefix.$i"
                val url = "http://$ip:$port/ping"

                try {
                    val req = Request.Builder().url(url).build()
                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val bodyText = resp.body?.string().orEmpty().trim()
                            val parts = bodyText.split(";")
                            val pcName = if (parts.size >= 2) parts[1] else "Wadatgy"
                            found.add(FoundServer(ip, pcName))
                        }
                    }
                } catch (_: Exception) {
                }
            }

            callback(found)
        }
    }

    private fun getLocalBasePrefix(context: Context): String? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return null
            val linkProps = cm.getLinkProperties(network) ?: return null

            val ipv4 = linkProps.linkAddresses
                .map { it.address }
                .firstOrNull { it is Inet4Address }
                ?.hostAddress ?: return null

            val parts = ipv4.split(".")
            if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}" else null
        } catch (_: Exception) {
            null
        }
    }
}
