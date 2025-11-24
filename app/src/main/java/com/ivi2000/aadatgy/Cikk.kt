package com.ivi2000.aadatgy

data class Cikk(
    val cikkod: String,
    val vonalkod: String?,
    val nev: String?,
    val kiszereles: String?,
    val keszlet: Double,
    val afa: Int,
    val fogyAr: Double,
    val akcAr: Double,
    val specAr: Double,
    val karton: Int
)
