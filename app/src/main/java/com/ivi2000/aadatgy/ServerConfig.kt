package com.ivi2000.aadatgy

import android.content.Context
import androidx.core.content.edit

object ServerConfig {

    private const val PREF_NAME = "server_settings"

    private const val KEY_IP = "server_ip"
    private const val KEY_PORT = "server_port"
    private const val KEY_NAME = "server_name"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getServerIp(ctx: Context): String? {
        return prefs(ctx).getString(KEY_IP, null)
    }

    fun setServerIp(ctx: Context, ip: String) {
        prefs(ctx).edit {
            putString(KEY_IP, ip)
        }
    }

    fun getServerPort(ctx: Context): Int {
        return prefs(ctx).getInt(KEY_PORT, 697)   // alapértelmezett port
    }

    fun setServerPort(ctx: Context, port: Int) {
        prefs(ctx).edit {
            putInt(KEY_PORT, port)
        }
    }

    fun getServerName(ctx: Context): String? {
        return prefs(ctx).getString(KEY_NAME, null)
    }

    fun setServerName(ctx: Context, name: String) {
        prefs(ctx).edit {
            putString(KEY_NAME, name)
        }
    }
}
