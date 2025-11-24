package com.ivi2000.aadatgy

import android.content.Context

class CikktorzsRepository(context: Context) {

    private val dbHelper = CikktorzsDbHelper(context)

    fun findByCode(code: String): Cikk? {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT cikkod, vonalkod, nev, kiszereles, keszlet, afa, fogy_ar, akc_ar, spec_ar, karton
            FROM cikk
            WHERE vonalkod = ?
            LIMIT 1
        """.trimIndent()

        db.rawQuery(sql, arrayOf(code)).use { cursor ->
            if (cursor.moveToFirst()) {
                val cikk = Cikk(
                    cikkod = cursor.getString(0),
                    vonalkod = cursor.getString(1),
                    nev = cursor.getString(2),
                    kiszereles = cursor.getString(3),
                    keszlet = cursor.getDouble(4),
                    afa = cursor.getInt(5),
                    fogyAr = cursor.getDouble(6),
                    akcAr = cursor.getDouble(7),
                    specAr = cursor.getDouble(8),
                    karton = cursor.getInt(9)
                )
                db.close()
                return cikk
            }
        }

        db.close()
        return null
    }
}
