package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File


data class Weapon(
    val file: String,
    val keyword: String,
    val name: String,
    val page: String
) {
    fun getWikiLink() =
        if (name.capitalize() == page) "[[${name.capitalize()}]]"
        else "[[$page|${name.capitalize()}]]"


    companion object {
        private val weapons = Klaxon().parseArray<Weapon>(File("weapons.json").inputStream())!!
            .map { Pair(it.keyword.toLowerCase(), it) }
            .toMap()

        fun get(keyword: String) = weapons[keyword.toLowerCase()]
    }
}
