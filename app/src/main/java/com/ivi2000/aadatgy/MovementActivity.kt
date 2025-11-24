package com.ivi2000.aadatgy

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import kotlin.concurrent.thread

class MovementActivity : BaseActivity() {

    companion object {
        const val EXTRA_TYPE = "movement_type"
    }

    private lateinit var repo: MovementRepository
    private lateinit var cikktorzsRepo: CikktorzsRepository
    private lateinit var type: MovementType

    private lateinit var txtTitle: TextView
    private lateinit var edtKod: EditText
    private lateinit var edtQty: EditText
    private lateinit var btnScan: Button
    private lateinit var btnAdd: Button
    private lateinit var btnExport: Button
    private lateinit var txtCurrentInfo: TextView
    private lateinit var txtSummary: TextView
    private lateinit var listView: ListView

    private val listAdapterItems = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var currentLines = listOf<MovementLine>()

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val code = result.data?.getStringExtra("SCANNED_CODE")
            if (!code.isNullOrEmpty()) {
                edtKod.setText(code)
                edtQty.requestFocus()
                loadExistingForCikk(code)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movement)

        val tName = intent.getStringExtra(EXTRA_TYPE) ?: "RENDELES"
        type = if (tName == "LELTAR") MovementType.LELTAR else MovementType.RENDELES

        repo = MovementRepository(this)
        cikktorzsRepo = CikktorzsRepository(this)

        txtTitle = findViewById(R.id.txtTitle)
        edtKod = findViewById(R.id.edtKod)
        edtQty = findViewById(R.id.edtQty)
        btnScan = findViewById(R.id.btnScan)
        btnAdd = findViewById(R.id.btnAdd)
        btnExport = findViewById(R.id.btnExport)
        txtCurrentInfo = findViewById(R.id.txtCurrentInfo)
        txtSummary = findViewById(R.id.txtSummary)
        listView = findViewById(R.id.listLines)

        txtTitle.text = when (type) {
            MovementType.RENDELES -> getString(R.string.move_title_rendeles)
            MovementType.LELTAR -> getString(R.string.move_title_leltar)
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listAdapterItems)
        listView.adapter = adapter

        listView.setOnItemLongClickListener { _, _, position, _ ->
            confirmDelete(currentLines[position])
            true
        }

        btnScan.setOnClickListener {
            val intent = Intent(this, BarcodeScanActivity::class.java)
            scanLauncher.launch(intent)
        }

        btnAdd.setOnClickListener { addOrUpdateLine() }
        btnExport.setOnClickListener { exportAndClear() }

        loadAllLines()
    }

    private fun loadAllLines() {
        thread {
            val lines = repo.getAll(type)
            runOnUiThread {
                currentLines = lines
                refreshList()
            }
        }
    }

    private fun loadExistingForCikk(codeOrBarcode: String) {
        thread {
            val cikk = cikktorzsRepo.findByCode(codeOrBarcode)
            runOnUiThread {
                if (cikk != null) {
                    val existing = repo.getForCikk(type, cikk.cikkod)
                    val baseText = if (!cikk.nev.isNullOrEmpty()) {
                        "${cikk.cikkod} - ${cikk.nev}"
                    } else {
                        cikk.cikkod
                    }

                    txtCurrentInfo.text = if (existing != null) {
                        baseText + "\n" + getString(R.string.move_existing_qty, existing.qty)
                    } else {
                        baseText + "\n" + getString(R.string.move_no_existing_qty)
                    }
                } else {
                    txtCurrentInfo.text = getString(R.string.move_not_found)
                }
            }
        }
    }

    private fun addOrUpdateLine() {
        val codeInput = edtKod.text.toString().trim()
        val qtyStr = edtQty.text.toString().trim().replace(",", ".")

        if (codeInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.move_no_code), Toast.LENGTH_SHORT).show()
            return
        }
        if (qtyStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.move_no_qty), Toast.LENGTH_SHORT).show()
            return
        }

        val qtyBase = qtyStr.toDoubleOrNull()
        if (qtyBase == null || qtyBase <= 0.0) {
            Toast.makeText(this, getString(R.string.move_qty_positive), Toast.LENGTH_SHORT).show()
            return
        }

        thread {
            val cikk = cikktorzsRepo.findByCode(codeInput)
            if (cikk == null) {
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.move_not_found), Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                val multiplier = if (cikk.karton > 0) cikk.karton else 1
                val finalQty = qtyBase * multiplier

                repo.addOrIncrease(type, cikk.cikkod, finalQty)

                runOnUiThread {
                    edtKod.text.clear()
                    edtQty.text.clear()

                    txtCurrentInfo.text = if (multiplier > 1) {
                        getString(
                            R.string.move_last_added_carton,
                            cikk.cikkod,
                            cikk.nev ?: "",
                            qtyBase
                        )
                    } else {
                        getString(
                            R.string.move_last_added,
                            cikk.cikkod,
                            cikk.nev ?: "",
                            finalQty
                        )
                    }

                    loadAllLines()
                }
            }
        }
    }

    private fun refreshList() {
        listAdapterItems.clear()
        var total = 0.0

        for (line in currentLines) {
            val cikk = cikktorzsRepo.findByCode(line.cikkod)
            val name = cikk?.nev ?: ""
            listAdapterItems.add(
                getString(R.string.move_list_item, line.cikkod, line.qty, name)
            )
            total += line.qty
        }

        adapter.notifyDataSetChanged()
        txtSummary.text = getString(R.string.move_total, total)
    }

    private fun confirmDelete(line: MovementLine) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.move_delete_title))
            .setMessage(getString(R.string.move_delete_message, line.cikkod, line.qty))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                thread {
                    repo.delete(type, line.cikkod)
                    runOnUiThread { loadAllLines() }
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun exportAndClear() {
        if (currentLines.isEmpty()) {
            Toast.makeText(this, getString(R.string.move_no_items), Toast.LENGTH_LONG).show()
            return
        }

        val ip = ServerConfig.getServerIp(this)
        val port = ServerConfig.getServerPort(this)

        if (ip.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.move_no_server), Toast.LENGTH_LONG).show()
            return
        }

        val fileName = when (type) {
            MovementType.RENDELES -> "rendeles.txt"
            MovementType.LELTAR -> "leltar.txt"
        }

        try {
            val file = java.io.File(filesDir, fileName)
            repo.generateTxt(type, file)

            MovementUploader.upload(this, type, file, ip, port) { success, error ->
                runOnUiThread {
                    if (success) {
                        thread {
                            repo.clearAll(type)
                            runOnUiThread {
                                loadAllLines()
                                Toast.makeText(
                                    this,
                                    getString(R.string.move_sent, fileName, file.absolutePath),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.move_send_error, error ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.move_save_send_error, e.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
