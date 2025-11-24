package com.ivi2000.aadatgy

import android.content.ContentValues
import android.content.Context
import androidx.core.database.sqlite.transaction
import java.io.File
import kotlin.concurrent.thread

object CikktorzsImporter {

    private val FIELD_NAMES = listOf(
        "CKTKOD", "CKTNEV", "CKTKSZ", "CKTKEM", "CKTAFA",
        "CKTBAR", "CKTFAR", "CKTAKC", "CKTXAR", "CKTCIK", "CKTKAR"
    )

    private val FIELD_SPECS = mapOf(
        "CKTKOD" to Triple("C", 14, 0),
        "CKTNEV" to Triple("C", 46, 0),
        "CKTKSZ" to Triple("C", 3, 0),
        "CKTKEM" to Triple("N", 12, 3),
        "CKTAFA" to Triple("N", 2, 0),
        "CKTBAR" to Triple("N", 12, 2),
        "CKTFAR" to Triple("N", 12, 2),
        "CKTAKC" to Triple("N", 12, 2),
        "CKTXAR" to Triple("N", 12, 2),
        "CKTCIK" to Triple("C", 13, 0),
        "CKTKAR" to Triple("N", 6, 0),
    )

    private val FIELD_LENGTHS = FIELD_NAMES.map { name -> FIELD_SPECS[name]!!.second }
    private val RECORD_WIDTH = FIELD_LENGTHS.sum()

    fun rebuildFromLocalFile(
        context: Context,
        callback: (Boolean, Int, String?) -> Unit
    ) {
        val file = File(context.filesDir, "wplrad.txt")
        if (!file.exists()) {
            callback(false, 0, context.getString(R.string.err_no_wplrad))
            return
        }

        thread {
            var count = 0

            try {
                val dbHelper = CikktorzsDbHelper(context)
                val db = dbHelper.writableDatabase

                try {
                    db.transaction {
                        delete("cikk", null, null)

                        file.bufferedReader(Charsets.UTF_8).useLines { lines ->
                            lines.forEach { line ->
                                val rec = parseLine(line) ?: return@forEach

                                val values = ContentValues().apply {
                                    put("cikkod", rec["CKTCIK"] as String)
                                    put("vonalkod", rec["CKTKOD"] as String?)
                                    put("nev", rec["CKTNEV"] as String?)
                                    put("kiszereles", rec["CKTKSZ"] as String?)
                                    put("keszlet", (rec["CKTKEM"] as? Double) ?: 0.0)
                                    put("afa", (rec["CKTAFA"] as? Int) ?: 0)
                                    put("fogy_ar", (rec["CKTFAR"] as? Double) ?: 0.0)
                                    put("akc_ar", (rec["CKTAKC"] as? Double) ?: 0.0)
                                    put("spec_ar", (rec["CKTXAR"] as? Double) ?: 0.0)
                                    put("karton", (rec["CKTKAR"] as? Int) ?: 0)
                                }

                                insert("cikk", null, values)
                                count++
                            }
                        }
                    }
                } finally {
                    db.close()
                }

                callback(true, count, null)
            } catch (e: Exception) {
                callback(false, count, e.message)
            }
        }
    }

    private fun parseLine(line: String): Map<String, Any?>? {
        val trimmed = line.trimEnd('\r', '\n')
        if (trimmed.length < RECORD_WIDTH) return null

        var pos = 0
        val rec = mutableMapOf<String, Any?>()

        FIELD_NAMES.forEachIndexed { index, name ->
            val len = FIELD_LENGTHS[index]
            val raw = trimmed.substring(pos, pos + len).trim()
            pos += len

            val (type, _, dec) = FIELD_SPECS[name]!!

            val value: Any? = if (type == "C") {
                raw
            } else {
                if (raw.isEmpty()) {
                    null
                } else {
                    try {
                        if (dec == 0) {
                            raw.toDouble().toInt()
                        } else {
                            raw.replace(',', '.').toDouble()
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            rec[name] = value
        }

        val cktcik = rec["CKTCIK"] as? String
        if (cktcik.isNullOrEmpty()) return null

        return rec
    }
}
