package com.ivi2000.aadatgy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit

class MainActivity : BaseActivity() {

    private lateinit var btnCikktorzs: Button
    private lateinit var btnArellenorzes: Button
    private lateinit var btnServerSettings: Button
    private lateinit var txtCurrentServer: TextView

    private lateinit var btnMegrendeles: Button
    private lateinit var btnLeltar: Button

    private lateinit var btnLanguage: ImageButton
    private lateinit var btnTheme: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnMegrendeles = findViewById(R.id.btnMegrendeles)
        btnLeltar = findViewById(R.id.btnLeltar)
        btnCikktorzs = findViewById(R.id.btnCikktorzs)
        btnArellenorzes = findViewById(R.id.btnArellenorzes)
        btnServerSettings = findViewById(R.id.btnServerSettings)
        txtCurrentServer = findViewById(R.id.txtCurrentServer)
        btnLanguage = findViewById(R.id.btnLanguage)
        btnTheme = findViewById(R.id.btnTheme)

        val updater = CikktorzsUpdater(this)

        refreshFlagIcon()

        btnLanguage.setOnClickListener {
            val current = LanguageManager.getLanguage(this)
            val next = if (current == "hu") "en" else "hu"
            LanguageManager.setLanguage(this, next)
            recreate()
        }

        btnTheme.setOnClickListener {
            ThemeManager.nextTheme(this)
            recreate()
        }

        btnMegrendeles.setOnClickListener {
            val intent = Intent(this, MovementActivity::class.java)
            intent.putExtra(MovementActivity.EXTRA_TYPE, "RENDELES")
            startActivity(intent)
        }

        btnLeltar.setOnClickListener {
            val intent = Intent(this, MovementActivity::class.java)
            intent.putExtra(MovementActivity.EXTRA_TYPE, "LELTAR")
            startActivity(intent)
        }

        btnServerSettings.setOnClickListener {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
        }

        btnCikktorzs.setOnClickListener {
            val ip = ServerConfig.getServerIp(this)
            val port = ServerConfig.getServerPort(this)

            if (ip.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.err_need_server), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            updater.downloadWplrad(ip, port.toString()) { success, error ->
                runOnUiThread {
                    if (!success) {
                        Toast.makeText(
                            this,
                            getString(R.string.err_download, error ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this, getString(R.string.download_ok), Toast.LENGTH_SHORT)
                            .show()

                        CikktorzsImporter.rebuildFromLocalFile(this) { ok, count, err ->
                            runOnUiThread {
                                if (ok) {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.import_ok, count),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.import_error, err ?: "", count),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        btnArellenorzes.setOnClickListener {
            startActivity(Intent(this, ItemLookupActivity::class.java))
        }

        updateCurrentServerLabel()
    }

    override fun onResume() {
        super.onResume()
        updateCurrentServerLabel()
        refreshFlagIcon()
    }

    private fun updateCurrentServerLabel() {
        val ip = ServerConfig.getServerIp(this)
        val port = ServerConfig.getServerPort(this)

        txtCurrentServer.text =
            if (ip.isNullOrEmpty()) getString(R.string.no_server)
            else getString(R.string.server_label, ip, port)
    }

    private fun refreshFlagIcon() {
        val lang = LanguageManager.getLanguage(this)
        btnLanguage.setImageResource(
            if (lang == "hu") R.drawable.flag_hu else R.drawable.flag_en
        )
    }
}

object LanguageManager {
    private const val PREF_NAME = "settings"
    private const val KEY_LANG = "lang"

    fun getLanguage(context: Context): String {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_LANG, "hu") ?: "hu"
    }

    fun setLanguage(context: Context, lang: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit {
            putString(KEY_LANG, lang)
        }
    }
}

object LocaleHelper {
    fun applyLocale(context: Context): Context {
        val lang = LanguageManager.getLanguage(context)
        val locale = java.util.Locale.forLanguageTag(lang)

        java.util.Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
