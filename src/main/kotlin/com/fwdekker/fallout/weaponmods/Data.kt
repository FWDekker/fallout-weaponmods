package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.fwdekker.fallout.weaponmods.FormID
import mu.KLogging
import java.io.File


// TODO document this
data class GameDatabase(
    val files: List<ESM>,
    val wikiWeapons: List<WikiWeapon>,
    val models: List<Model>,
    val perks: List<Perk>,

    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>,
    val weapons: List<Weapon>
) {
    companion object : KLogging() {
        fun fromDirectory(directory: File): GameDatabase? {
            // TODO clean up
            val files = Klaxon().parseArray<ESM>(File("esms.json").inputStream())!!
            val models = Klaxon().parseArray<Model>(File("models.json").inputStream())!!
            val perks = Klaxon().parseArray<Perk>(File("perks.json").inputStream())!!

            var klaxon = Klaxon()
                .fieldConverter(ESM.Converter.Annotation::class,
                    ESM.Converter(files))
                .fieldConverter(FormID.Converter.Annotation::class, FormID.Converter())
                .fieldConverter(Perk.Converter.Annotation::class,
                    Perk.Converter(perks))
                .fieldConverter(Model.Converter.Annotation::class,
                    Model.Converter(models))
                .fieldConverter(CraftableObject.ConditionConverter.Annotation::class,
                    CraftableObject.ConditionConverter(perks))

            val wikiWeapons = klaxon.parseArray<WikiWeapon>(File("weapons.json").inputStream())!!
            klaxon = klaxon
                .fieldConverter(WikiWeapon.LinkConverter.Annotation::class,
                    WikiWeapon.LinkConverter(wikiWeapons))

            // TODO throw exceptions
            val components = klaxon.parseArray<Component>(File(directory, "cmpo.json").inputStream())!!
            klaxon = klaxon.fieldConverter(Component.Converter.Annotation::class,
                Component.Converter(components))
            klaxon = klaxon.fieldConverter(CraftableObject.ComponentConverter.Annotation::class,
                CraftableObject.ComponentConverter(components))

            val weapons = klaxon.parseArray<Weapon>(File(directory, "weap.json").inputStream())!!
            weapons.forEach { weapon ->
                val wikiWeapon = wikiWeapons.singleOrNull { it.formID == weapon.formID }
                weapon.wikiLink = wikiWeapon?.link
                weapon.keyword = wikiWeapon?.keyword
            }
            klaxon = klaxon.fieldConverter(Weapon.Converter.Annotation::class,
                Weapon.Converter(weapons))

            val looseMods = klaxon.parseArray<LooseMod>(File(directory, "misc.json").inputStream())!!
            klaxon = klaxon.fieldConverter(LooseMod.Converter.Annotation::class,
                LooseMod.Converter(looseMods))

            val objectModifiers = klaxon.parseArray<ObjectModifier>(File(directory, "omod.json").inputStream())!!
            klaxon = klaxon.fieldConverter(ObjectModifier.Converter.Annotation::class,
                ObjectModifier.Converter(objectModifiers))

            val craftableObjects = klaxon.parseArray<CraftableObject>(File(directory, "cobj.json").inputStream())!!

            return GameDatabase(
                files,
                wikiWeapons,
                models,
                perks,

                looseMods,
                objectModifiers,
                craftableObjects,
                components,
                weapons
            )
        }
    }
}


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
data class WikiWeapon(
    @ESM.Converter.Annotation
    val file: ESM,
    val keyword: String,
    @FormID.Converter.Annotation
    val formID: FormID,
    val name: String,
    val page: String
) {
    val link = Link(page, name)


    class LinkConverter(val weapons: List<WikiWeapon>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == Weapon::class.java

        override fun fromJson(jv: JsonValue) =
            weapons.singleOrNull { it.keyword.equals(jv.string, ignoreCase = true) }?.link

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
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

/**
 * A component. (CMPO)
 *
 * @property file the ESM in which the component is defined
 * @property formID the base ID of the component
 * @property editorID the editor ID of the component
 * @property name the name of the component
 */
data class Component(
    @ESM.Converter.Annotation
    val file: ESM,
    @FormID.Converter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String
) {
    class Converter(val components: List<Component>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == Component::class.java

        override fun fromJson(jv: JsonValue) =
            components.singleOrNull { it.editorID.equals(jv.string, ignoreCase = true) }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}

/**
 * The weapon mod as seen in the player character's inventory. (MISC)
 *
 * @property file the ESM in which the weapon mod is defined
 * @property formID the base ID of the weapon mod
 * @property editorID the editor ID of the weapon mod
 * @property name the name of the weapon mod
 * @property value the value of the weapon mod in bottle caps
 * @property weight the weight of the weapon mod in pounds
 * @property model the path to the in-game model file for the weapon mod
 */
data class LooseMod(
    @ESM.Converter.Annotation
    val file: ESM,
    @FormID.Converter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String,
    val value: Int,
    val weight: Double,
    @Model.Converter.Annotation
    val model: Model? = null
) {
    class Converter(val looseMods: List<LooseMod>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == LooseMod::class.java

        override fun fromJson(jv: JsonValue) =
            looseMods.singleOrNull { it.editorID.equals(jv.string, ignoreCase = true) }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}

/**
 * The effects of the weapon mod. (OMOD)
 *
 * @property file the ESM in which the weapon mod is defined
 * @property formID the base ID of the weapon mod
 * @property editorID the editor ID of the weapon mod
 * @property name the name of the weapon mod
 * @property description the effects of the weapon mod
 * @property looseMod the editor ID of the corresponding [LooseMod]
 * @property weapon the keyword of the weapon to which the effects can be applied
 */
data class ObjectModifier(
    @ESM.Converter.Annotation
    val file: ESM,
    @FormID.Converter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String,
    val description: String,
    @LooseMod.Converter.Annotation
    val looseMod: LooseMod? = null,
    @Weapon.Converter.Annotation
    val weapon: Weapon? = null, // TODO rename to "weaponKeyword"
    val effects: List<Effect>
) {
    data class Effect(
        val valueType: String,
        val functionType: String,
        val property: String,
        val value1: Any,
        val value2: Any,
        val step: Float
    )


    class Converter(val objectModifiers: List<ObjectModifier>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == ObjectModifier::class.java

        override fun fromJson(jv: JsonValue) =
            objectModifiers.singleOrNull { it.editorID.equals(jv.string, ignoreCase = true) }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}

/**
 * The recipe for the weapon mod. (COBJ)
 *
 * @property file the ESM in which the recipe is defined
 * @property formID the base ID of the recipe
 * @property editorID the editor ID of the recipe
 * @property createdMod the editor ID of the [ObjectModifier] that is created by this recipe
 * @property components the components required to use this recipe
 * @property conditions the requirements (e.g. perks) to use this recipe
 */
data class CraftableObject(
    @ESM.Converter.Annotation
    val file: ESM,
    @FormID.Converter.Annotation
    val formID: FormID,
    val editorID: String,
    @ObjectModifier.Converter.Annotation
    val createdMod: ObjectModifier? = null,
    @ComponentConverter.Annotation
    val components: Map<Component, Int>,
    @ConditionConverter.Annotation
    val conditions: Map<Perk, Int> // TODO rename to "requirements"
) {
    class ComponentConverter(val components: List<Component>) : Converter {
        override fun canConvert(cls: Class<*>) = cls == Map::class.java

        override fun fromJson(jv: JsonValue): Map<Component, Int> {
            val ja = jv.array!!

            return ja.filterIsInstance<JsonObject>()
                .filter { jo -> components.any { it.editorID == jo.string("component") } } // TODO log if filtered out
                .map { jo ->
                    Pair(
                        components.single { it.editorID == jo.string("component") },
                        jo.int("count")!!
                    )
                }
                .toMap()
        }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }

    class ConditionConverter(val perks: List<Perk>) : Converter {
        override fun canConvert(cls: Class<*>) = cls == Map::class.java

        override fun fromJson(jv: JsonValue): Map<Perk, Int> {
            val ja = jv.array!!

            return ja.filterIsInstance<JsonObject>()
                .map { jo ->
                    Pair(
                        perks.singleOrNull { it.editorID == jo.string("perk") }
                            ?: error("Could not find perk `${jo.string("perk")}`."),
                        jo.int("rank")!!
                    )
                }
                .toMap()
        }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}

/**
 * A weapon. (WEAP)
 *
 * @property file the ESM in which the weapon is defined
 * @property formID the base ID of the weapon
 * @property editorID the editor ID of the weapon
 * @property name the name of the weapon
 * @property speed the speed at which the weapon fires
 * @property reloadSpeed the speed at which the weapon reloads
 * @property reach the reach of the weapon
 * @property minRange the minimum range of the weapon
 * @property maxRange the maximum range of the weapon
 * @property attackDelay the number of seconds in between consecutive uses of the weapon
 * @property weight the weight of the weapon in pounds
 * @property value the value of the weapon in bottle caps
 */
data class Weapon(
    @ESM.Converter.Annotation
    val file: ESM,
    @FormID.Converter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String,
    @Json(ignored = true) // TODO Find way to instantiate these fields directly
    var keyword: String? = null,
    @Json(ignored = true)
    var wikiLink: Link? = null,
    val speed: Double,
    val reloadSpeed: Double,
    val reach: Double,
    val minRange: Double,
    val maxRange: Double,
    val attackDelay: Double,
    val weight: Double,
    val value: Int,
    val baseDamage: Int
) {
    class Converter(val weapons: List<Weapon>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == Weapon::class.java

        override fun fromJson(jv: JsonValue) = weapons.singleOrNull { it.keyword.equals(jv.string, ignoreCase = true) }

        override fun toJson(value: Any) = error("Cannot convert to JSON.")


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}
