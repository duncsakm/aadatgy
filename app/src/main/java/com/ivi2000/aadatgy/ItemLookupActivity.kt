package com.ivi2000.aadatgy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class ItemLookupActivity : BaseActivity() {

    private lateinit var repo: CikktorzsRepository

    private lateinit var edtKod: EditText
    private lateinit var btnKeres: Button
    private lateinit var btnScan: Button

    private lateinit var txtNev: TextView
    private lateinit var txtKodok: TextView
    private lateinit var txtKiszereles: TextView
    private lateinit var txtArak: TextView
    private lateinit var txtAfa: TextView
    private lateinit var txtKeszlet: TextView
    private lateinit var txtKarton: TextView

    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(null)
        } else {
            super.attachBaseContext(LocaleHelper.applyLocale(newBase))
        }
    }

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val code = result.data?.getStringExtra("SCANNED_CODE")
            if (!code.isNullOrEmpty()) {
                edtKod.setText(code)
                keres()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_lookup)

        repo = CikktorzsRepository(this)

        edtKod = findViewById(R.id.edtKod)
        btnKeres = findViewById(R.id.btnKeres)
        btnScan = findViewById(R.id.btnScan)

        txtNev = findViewById(R.id.txtNev)
        txtKodok = findViewById(R.id.txtKodok)
        txtKiszereles = findViewById(R.id.txtKiszereles)
        txtArak = findViewById(R.id.txtArak)
        txtAfa = findViewById(R.id.txtAfa)
        txtKeszlet = findViewById(R.id.txtKeszlet)
        txtKarton = findViewById(R.id.txtKarton)

        resetFields()

        btnKeres.setOnClickListener { keres() }

        edtKod.setOnEditorActionListener { _, _, _ ->
            keres()
            true
        }

        btnScan.setOnClickListener {
            val intent = Intent(this, BarcodeScanActivity::class.java)
            scanLauncher.launch(intent)
        }
    }

    private fun resetFields() {
        val unknown = getString(R.string.lookup_unknown)

        txtNev.text = getString(R.string.lookup_name, unknown)
        txtKodok.text = getString(R.string.lookup_codes, unknown, unknown)
        txtKiszereles.text = getString(R.string.lookup_pack, unknown)
        txtArak.text = getString(R.string.lookup_price_label)
        txtAfa.text = getString(R.string.lookup_afa, 0)
        txtKeszlet.text = getString(R.string.lookup_stock, 0.0)
        txtKarton.text = getString(R.string.lookup_carton, 0)
    }

    private fun keres() {
        val code = edtKod.text.toString().trim()
        if (code.isEmpty()) {
            Toast.makeText(this, getString(R.string.lookup_enter_code), Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            val cikk = repo.findByCode(code)

            runOnUiThread {
                if (cikk == null) {
                    Toast.makeText(this, getString(R.string.lookup_no_result), Toast.LENGTH_LONG)
                        .show()
                    resetFields()
                } else {
                    val nev = cikk.nev ?: getString(R.string.lookup_unknown)
                    val vonalkod = cikk.vonalkod ?: getString(R.string.lookup_unknown)
                    val kiszereles = cikk.kiszereles ?: getString(R.string.lookup_unknown)

                    txtNev.text = getString(R.string.lookup_name, nev)
                    txtKodok.text = getString(R.string.lookup_codes, cikk.cikkod, vonalkod)
                    txtKiszereles.text = getString(R.string.lookup_pack, kiszereles)

                    val alapAr = cikk.fogyAr
                    val akcAr = cikk.akcAr
                    val specAr = cikk.specAr

                    val arSzoveg = buildString {
                        append(getString(R.string.lookup_price, alapAr))
                        if (akcAr > 0.0 && akcAr != alapAr) {
                            append("  |  ${getString(R.string.lookup_price_akc, akcAr)}")
                        }
                        if (specAr > 0.0 && specAr != alapAr && specAr != akcAr) {
                            append("  |  ${getString(R.string.lookup_price_spec, specAr)}")
                        }
                    }

                    txtArak.text = arSzoveg
                    txtAfa.text = getString(R.string.lookup_afa, cikk.afa)
                    txtKeszlet.text = getString(R.string.lookup_stock, cikk.keszlet)
                    txtKarton.text = getString(R.string.lookup_carton, cikk.karton)
                }
            }
        }.start()
    }
}
