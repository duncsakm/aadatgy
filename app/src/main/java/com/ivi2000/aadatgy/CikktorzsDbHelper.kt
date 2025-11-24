package com.ivi2000.aadatgy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CikktorzsDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cikk (
                vonalkod TEXT PRIMARY KEY,
                cikkod  TEXT NOT NULL,
                kartonkod TEXT,
                nev TEXT,
                kiszereles TEXT,
                keszlet REAL,
                afa INTEGER,
                fogy_ar REAL,
                akc_ar REAL,
                spec_ar REAL,
                karton INTEGER
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX cikkod_idx ON cikk(cikkod)")

        db.execSQL(
            """
            CREATE TABLE rendeles (
                cikkod TEXT PRIMARY KEY,
                qty REAL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE leltar (
                cikkod TEXT PRIMARY KEY,
                qty REAL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cikk")
        db.execSQL("DROP TABLE IF EXISTS rendeles")
        db.execSQL("DROP TABLE IF EXISTS leltar")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "cikktorzs.db"
        private const val DATABASE_VERSION = 5
    }
}
