package com.fwdekker.fallout.weaponmods.wiki

import com.beust.klaxon.JsonValue
import com.fwdekker.fallout.weaponmods.FormID


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


    class Converter(val files: List<ESM>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == ESM::class.java

        override fun fromJson(jv: JsonValue) =
            files.singleOrNull { it.fileName.equals(jv.string, ignoreCase = true) }
                ?: error("Could not find ESM `${jv.string}`.")

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
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
    @ESM.Converter.Annotation
    val file: ESM,
    val keyword: String,
    @FormID.Converter.Annotation
    val formID: FormID,
    val name: String,
    val page: String
) {
    val link = Link(page, name)
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
    class Converter(val models: List<Model>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == Model::class.java

        override fun fromJson(jv: JsonValue) = models.singleOrNull { it.model.equals(jv.string, ignoreCase = true) }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
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
    class Converter(val perks: List<Perk>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == Perk::class.java

        override fun fromJson(jv: JsonValue) =
            perks.singleOrNull { it.editorID.equals(jv.string, ignoreCase = true) }
                ?: error("Could not find perk `${jv.string}`.")

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}
