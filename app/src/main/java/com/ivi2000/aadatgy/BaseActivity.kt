package com.ivi2000.aadatgy

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(null)
        } else {
            val localized = LocaleHelper.applyLocale(newBase)
            super.attachBaseContext(localized)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
    }
}
