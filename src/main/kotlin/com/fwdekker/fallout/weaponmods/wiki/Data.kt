package com.fwdekker.fallout.weaponmods.wiki

import com.beust.klaxon.Klaxon
import com.fwdekker.fallout.weaponmods.FormID
import com.fwdekker.fallout.weaponmods.xedit.ESMConverter
import com.fwdekker.fallout.weaponmods.xedit.FormIDConverter
import java.io.File


/**
 * An ESM as described on Nukapedia.
 *
 * Essentially, this class describes both the base game and its add-ons.
 *
 * @property fileName the name of the ESM file
 * @property name the name of the game/add-on the ESM corresponds to
 * @property page the page on Nukapedia that describes this game/add-on
 * @property abbreviation the abbreviation that is used on Nukapedia to describe this game/add-on
 * @property modCategory the category on Nukapedia for weapon mods in this game/add-on
 */
data class ESM(
    val fileName: String,
    val name: String,
    val page: String,
    val abbreviation: String,
    val modCategory: String? = null
) {
    val link = Link(page, name)


    companion object {
        // TODO move JSON to suitable location (same for Weapons)
        // TODO handle errors (same for Weapons)
        private val esms = Klaxon().parseArray<ESM>(File("esms.json").inputStream())!!
            .map { Pair(it.fileName.toLowerCase(), it) }
            .toMap()

        /**
         * Returns the [ESM] corresponding to the given file name.
         *
         * @param fileName the name of the ESM file
         * @return the [ESM] corresponding to the given file name
         */
        fun get(fileName: String) = esms[fileName.toLowerCase()]
    }
}

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
    @ESMConverter.Annotation
    val file: ESM,
    val keyword: String,
    @FormIDConverter.Annotation
    val formID: FormID,
    val name: String,
    val page: String
) {
    val link = Link(page, name)


    companion object {
        val default = Weapon( // TODO remove this
            file = ESM.get("Fallout4.esm")!!,
            keyword = "NULL",
            formID = FormID("000000"),
            name = "NULL",
            page = "NULL"
        )

        private val weapons = Klaxon()
            .fieldConverter(ESMConverter.Annotation::class, ESMConverter())
            .fieldConverter(FormIDConverter.Annotation::class, FormIDConverter())
            .parseArray<Weapon>(File("weapons.json").inputStream())!!
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
// TODO this can just be a map, no need for a class
data class Model(
    val model: String,
    val image: String
) {
    companion object {
        val default = Model("NULL", "NULL") // TODO delete this

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

/**
 * Maps a perk editor ID to its rank and a Nukapedia page.
 *
 * @property editorID the editor ID of the perk, excluding the rank
 * @property name the name of the perk
 * @property page the Nukapedia page for the perk
 */
data class Perk(
    val editorID: String,
    val name: String,
    val page: String
) {
    companion object {
        val default = Perk("NULL", "NULL", "NULL")

        private val perks = Klaxon().parseArray<Perk>(File("perks.json").inputStream())!!
            .map { Pair(it.editorID.toLowerCase(), it) }
            .toMap()

        /**
         * Returns the [Perk] corresponding to the given editor ID.
         *
         * @param editorID the editor ID of the perk, excluding the rank
         * @return the [Perk] corresponding to the given editor ID
         */
        fun get(editorID: String) = perks[editorID.toLowerCase()]
    }
}
