package com.fwdekker.fallout.weaponmods.xedit

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.fwdekker.fallout.weaponmods.FormID
import com.fwdekker.fallout.weaponmods.wiki.ESM
import com.fwdekker.fallout.weaponmods.wiki.Model
import com.fwdekker.fallout.weaponmods.wiki.Perk
import mu.KLogging
import java.io.File


// TODO document this
data class GameDatabase(
    val files: List<ESM>,
    val wikiWeapons: List<com.fwdekker.fallout.weaponmods.wiki.Weapon>,
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
            val files = Klaxon().parseArray<ESM>(File("esms.json").inputStream())!!
            val models = Klaxon().parseArray<Model>(File("models.json").inputStream())!!
            val perks = Klaxon().parseArray<Perk>(File("perks.json").inputStream())!!

            var klaxon = Klaxon()
                .fieldConverter(ESM.Converter.Annotation::class, ESM.Converter(files))
                .fieldConverter(FormID.Converter.Annotation::class, FormID.Converter())
                .fieldConverter(Perk.Converter.Annotation::class, Perk.Converter(perks))
                .fieldConverter(Model.Converter.Annotation::class, Model.Converter(models))
                .fieldConverter(CraftableObject.ConditionConverter.Annotation::class,
                    CraftableObject.ConditionConverter(perks))

            val wikiWeapons =
                klaxon.parseArray<com.fwdekker.fallout.weaponmods.wiki.Weapon>(File("weapons.json").inputStream())!!

            // TODO throw exceptions
            val components = klaxon.parseArray<Component>(File(directory, "cmpo.json").inputStream())!!
            klaxon = klaxon.fieldConverter(Component.Converter.Annotation::class, Component.Converter(components))
            klaxon = klaxon.fieldConverter(CraftableObject.ComponentConverter.Annotation::class,
                CraftableObject.ComponentConverter(components))

            val weapons = klaxon.parseArray<Weapon>(File(directory, "weap.json").inputStream())!!
            klaxon = klaxon.fieldConverter(Weapon.Converter.Annotation::class, Weapon.Converter(wikiWeapons))

            val looseMods = klaxon.parseArray<LooseMod>(File(directory, "misc.json").inputStream())!!
            klaxon = klaxon.fieldConverter(LooseMod.Converter.Annotation::class, LooseMod.Converter(looseMods))

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

        override fun toJson(value: Any) = "\"${(value as Component).editorID}\""


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

        override fun toJson(value: Any) = "\"${(value as LooseMod).editorID}\""


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
    val weapon: com.fwdekker.fallout.weaponmods.wiki.Weapon? = null,
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

        override fun toJson(value: Any) = "\"${(value as ObjectModifier).editorID}\""


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
    class Converter(val weapons: List<com.fwdekker.fallout.weaponmods.wiki.Weapon>) : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == Weapon::class.java

        override fun fromJson(jv: JsonValue) = weapons.singleOrNull { it.keyword.equals(jv.string, ignoreCase = true) }

        override fun toJson(value: Any) = "\"${(value as com.fwdekker.fallout.weaponmods.wiki.Weapon).keyword}\""


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }
}
