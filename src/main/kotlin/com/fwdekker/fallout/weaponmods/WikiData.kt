package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File


/**
 * A weapon on Nukapedia.
 *
 * @property file the name of the ESM in which the weapon is defined
 * @property keyword the keyword for this weapon in the Creation Kit
 * @property name the name of the weapon as it should appear in written text
 * @property page the page on Nukapedia that describes the weapon
 * @property link a [Link] object for this weapon
 */
data class Weapon(
    val file: String,
    val keyword: String,
    val name: String,
    val page: String
) {
    val link = Link(page, name)


    companion object {
        private val weapons = Klaxon().parseArray<Weapon>(File("weapons.json").inputStream())!!
            .map { Pair(it.keyword.toLowerCase(), it) }
            .toMap()

        /**
         * Returns the [Weapon] corresponding to the given keyword.
         *
         * @param keyword the keyword for the weapon in the Creation Kit
         * @return the [Weapon] corresponding to the given keyword
         */
        fun get(keyword: String) = weapons[keyword.toLowerCase()]
    }
}

/**
 * Maps an in-game model to a Nukapedia image.
 *
 * @property model the path of the in-game model used for the weapon mod
 * @property image the path on Nukapedia for the image to display for the model
 */
data class Model(
    val model: String,
    val image: String
) {
    companion object {
        private val models = Klaxon().parseArray<Model>(File("models.json").inputStream())!!
            .map { Pair(it.model.toLowerCase(), it) }
            .toMap()

        /**
         * Returns the [Model] corresponding to the given in-game model.
         *
         * @param model the path of the in-game model used for the weapon mod
         * @return the [Model] corresponding to the given in-game model
         */
        fun get(model: String) = models[model.toLowerCase()]
    }
}
