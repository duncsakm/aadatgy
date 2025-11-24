package com.ivi2000.aadatgy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject

class ServerSettingsActivity : BaseActivity() {

    private lateinit var tvCurrentServer: TextView
    private lateinit var btnFindServer: Button
    private lateinit var btnSetServerQr: Button
    private lateinit var btnSetServerManual: Button

    private val qrLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

                val jsonStr = result.data?.getStringExtra("SERVER_QR_DATA")
                    ?: return@registerForActivityResult

                try {
                    val obj = JSONObject(jsonStr)

                    if (obj.getString("type") != "WADATGY_SERVER") {
                        Toast.makeText(
                            this,
                            getString(R.string.server_qr_not_server),
                            Toast.LENGTH_LONG
                        ).show()
                        return@registerForActivityResult
                    }

                    val name = obj.getString("PcName")
                    val ip = obj.getString("IpAddress")
                    val port = obj.getString("LocalPort").toInt()

                    ServerConfig.setServerIp(this, ip)
                    ServerConfig.setServerPort(this, port)
                    ServerConfig.setServerName(this, name)

                    Toast.makeText(
                        this,
                        getString(R.string.server_set_success, ip, port),
                        Toast.LENGTH_LONG
                    ).show()

                    updateCurrentServerLabel()

                } catch (_: Exception) {
                    Toast.makeText(this, getString(R.string.server_qr_invalid), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_settings)

        tvCurrentServer = findViewById(R.id.tvCurrentServer)
        btnFindServer = findViewById(R.id.btnFindServer)
        btnSetServerQr = findViewById(R.id.btnSetServerQr)
        btnSetServerManual = findViewById(R.id.btnSetServerManual)

        updateCurrentServerLabel()

        btnSetServerQr.setOnClickListener {
            val intent = Intent(this, BarcodeScanActivity::class.java)
            intent.putExtra("MODE", "SERVER_QR")
            qrLauncher.launch(intent)
        }

        btnSetServerManual.setOnClickListener {
            showManualServerDialog()
        }

        btnFindServer.setOnClickListener {
            findServerOnNetwork()
        }
    }

    override fun onResume() {
        super.onResume()
        updateCurrentServerLabel()
    }

    private fun updateCurrentServerLabel() {
        val ip = ServerConfig.getServerIp(this)
        val port = ServerConfig.getServerPort(this)

        tvCurrentServer.text =
            if (ip.isNullOrEmpty()) getString(R.string.server_no_server)
            else getString(R.string.server_label, ip, port)
    }

    private fun showManualServerDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_manual_server, null)

        val etIp = layout.findViewById<EditText>(R.id.etIp)
        val etPort = layout.findViewById<EditText>(R.id.etPort)

        etIp.setText(ServerConfig.getServerIp(this))
        etPort.setText(ServerConfig.getServerPort(this).toString())

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.server_manual_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val ip = etIp.text.toString().trim()
                val portStr = etPort.text.toString().trim()

                if (!isValidIp(ip)) {
                    Toast.makeText(this, getString(R.string.invalid_ip), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val port = portStr.toIntOrNull()
                if (port == null || port !in 1..65535) {
                    Toast.makeText(this, getString(R.string.invalid_port), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                ServerConfig.setServerIp(this, ip)
                ServerConfig.setServerPort(this, port)

                Toast.makeText(
                    this,
                    getString(R.string.server_set_success, ip, port),
                    Toast.LENGTH_LONG
                ).show()

                updateCurrentServerLabel()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun isValidIp(ip: String): Boolean {
        val regex = Regex(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])$"
        )
        return regex.matches(ip)
    }

    private fun findServerOnNetwork() {
        val port = ServerConfig.getServerPort(this)

        Toast.makeText(this, getString(R.string.network_searching), Toast.LENGTH_SHORT).show()

        ServerDiscovery.scanForServers(this, port) { list ->
            runOnUiThread {
                if (list.isEmpty()) {
                    Toast.makeText(this, getString(R.string.network_not_found), Toast.LENGTH_LONG)
                        .show()
                } else {
                    val items = list.map { "${it.name} (${it.ip})" }.toTypedArray()

                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.network_choose_server))
                        .setItems(items) { _, which ->
                            val chosen = list[which]
                            ServerConfig.setServerIp(this, chosen.ip)
                            updateCurrentServerLabel()

                            Toast.makeText(
                                this,
                                getString(R.string.network_set, chosen.name, chosen.ip),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
            }
        }
    }
}
