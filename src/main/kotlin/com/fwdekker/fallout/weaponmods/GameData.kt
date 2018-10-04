package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File


data class ESM(
    val fileName: String,
    val name: String,
    val link: String,
    val abbreviation: String,
    val modCategory: String? = null
) {
    fun getWikiLink() =
        if (name.capitalize() == link) "''[[$name]]''"
        else "''[[$link|$name]]''"


    companion object {
        // TODO move JSON to suitable location (same for Weapons)
        // TODO handle errors (same for Weapons)
        private val esms = Klaxon().parseArray<ESM>(File("esms.json").inputStream())!!
            .map { Pair(it.fileName.toLowerCase(), it) }
            .toMap()

        fun get(fileName: String) = esms[fileName.toLowerCase()]
    }
}
