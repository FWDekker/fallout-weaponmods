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

data class Model(
    val model: String,
    val image: String
) {
    companion object {
        private val models = Klaxon().parseArray<Model>(File("models.json").inputStream())!!
            .map { Pair(it.model.toLowerCase(), it) }
            .toMap()

        fun get(model: String) = models[model.toLowerCase()]
    }
}
