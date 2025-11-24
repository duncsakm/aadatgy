package com.ivi2000.aadatgy

import android.content.ContentValues
import android.content.Context
import androidx.core.database.sqlite.transaction
import java.io.File
import java.util.Locale

enum class MovementType {
    RENDELES,
    LELTAR
}

data class MovementLine(
    val cikkod: String,
    val qty: Double
)

class MovementRepository(context: Context) {

    private val dbHelper = CikktorzsDbHelper(context)

    private fun tableName(type: MovementType): String =
        when (type) {
            MovementType.RENDELES -> "rendeles"
            MovementType.LELTAR -> "leltar"
        }

    fun getAll(type: MovementType): List<MovementLine> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<MovementLine>()
        val tname = tableName(type)

        db.rawQuery(
            "SELECT cikkod, qty FROM $tname ORDER BY cikkod",
            null
        ).use { cur ->
            while (cur.moveToNext()) {
                val cikkod = cur.getString(0)
                val qty = cur.getDouble(1)
                list.add(MovementLine(cikkod, qty))
            }
        }

        db.close()
        return list
    }

    fun getForCikk(type: MovementType, cikkod: String): MovementLine? {
        val db = dbHelper.readableDatabase
        val tname = tableName(type)
        var result: MovementLine? = null

        db.rawQuery(
            "SELECT cikkod, qty FROM $tname WHERE cikkod = ?",
            arrayOf(cikkod)
        ).use { cur ->
            if (cur.moveToFirst()) {
                val code = cur.getString(0)
                val qty = cur.getDouble(1)
                result = MovementLine(code, qty)
            }
        }

        db.close()
        return result
    }

    fun addOrIncrease(type: MovementType, cikkod: String, qtyToAdd: Double) {
        if (qtyToAdd == 0.0) return

        val db = dbHelper.writableDatabase
        val tname = tableName(type)

        db.transaction {
            var current = 0.0

            rawQuery(
                "SELECT qty FROM $tname WHERE cikkod = ?",
                arrayOf(cikkod)
            ).use { cur ->
                if (cur.moveToFirst()) {
                    current = cur.getDouble(0)
                }
            }

            val newQty = current + qtyToAdd

            val values = ContentValues().apply {
                put("cikkod", cikkod)
                put("qty", newQty)
            }

            insertWithOnConflict(
                tname,
                null,
                values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
            )
        }

        db.close()
    }

    fun delete(type: MovementType, cikkod: String) {
        val db = dbHelper.writableDatabase
        val tname = tableName(type)
        db.delete(tname, "cikkod = ?", arrayOf(cikkod))
        db.close()
    }

    fun clearAll(type: MovementType) {
        val db = dbHelper.writableDatabase
        val tname = tableName(type)
        db.delete(tname, null, null)
        db.close()
    }

    fun generateTxt(type: MovementType, destFile: File) {
        val lines = getAll(type)
        val sb = StringBuilder()

        for (line in lines) {
            val cktcikField = line.cikkod.padEnd(13, ' ').take(13)
            val qtyField = String.format(Locale.US, "%12.3f", line.qty)

            sb.append(cktcikField)
            sb.append(' ')
            sb.append(qtyField)
            sb.append("\r\n")
        }

        destFile.writeText(sb.toString(), Charsets.UTF_8)
    }
}
