package com.fwdekker.fallout.weaponmods.xedit

import com.beust.klaxon.Converter
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
    val esms: List<ESM>,
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
            var klaxon = Klaxon()
                .fieldConverter(ESMConverter.Annotation::class, ESMConverter())
                .fieldConverter(FormIDConverter.Annotation::class, FormIDConverter())
                .fieldConverter(PerkConverter.Annotation::class, PerkConverter())
                .fieldConverter(ModelConverter.Annotation::class, ModelConverter())

            val esms = klaxon.parseArray<ESM>(File("esms.json").inputStream())!!
            val wikiWeapons =
                klaxon.parseArray<com.fwdekker.fallout.weaponmods.wiki.Weapon>(File("weapons.json").inputStream())!!
            val models = klaxon.parseArray<Model>(File("models.json").inputStream())!!
            val perks = klaxon.parseArray<Perk>(File("perks.json").inputStream())!!

            // TODO throw exceptions
            val components = klaxon.parseArray<Component>(File(directory, "cmpo.json").inputStream())!!
            klaxon = klaxon.fieldConverter(ComponentConverter.Annotation::class, ComponentConverter(components))

            val weapons = klaxon.parseArray<Weapon>(File(directory, "weap.json").inputStream())!!
            klaxon = klaxon.fieldConverter(WeaponConverter.Annotation::class, WeaponConverter())

            val looseMods = klaxon.parseArray<LooseMod>(File(directory, "misc.json").inputStream())!!
            klaxon = klaxon.fieldConverter(LooseModConverter.Annotation::class, LooseModConverter(looseMods))

            val objectModifiers = klaxon.parseArray<ObjectModifier>(File(directory, "omod.json").inputStream())!!
            klaxon = klaxon.fieldConverter(ObjectModifierConverter.Annotation::class,
                ObjectModifierConverter(objectModifiers))

            val craftableObjects = klaxon.parseArray<CraftableObject>(File(directory, "cobj.json").inputStream())!!

            return GameDatabase(
                esms,
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


class FormIDConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == FormID::class.java

    override fun fromJson(jv: JsonValue) = FormID.fromString(jv.string!!)

    override fun toJson(value: Any) = "\"${(value as FormID).id}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class ESMConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == ESM::class.java

    override fun fromJson(jv: JsonValue) = ESM.get(jv.string!!)!!

    override fun toJson(value: Any) = "\"${(value as ESM).fileName}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class LooseModConverter(val looseMods: List<LooseMod>) : Converter {
    override fun canConvert(cls: Class<*>) = cls == LooseMod::class.java

    override fun fromJson(jv: JsonValue) = looseMods.singleOrNull { it.editorID == jv.string }

    override fun toJson(value: Any) = "\"${(value as LooseMod).editorID}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class ObjectModifierConverter(val objectModifiers: List<ObjectModifier>) : Converter {
    override fun canConvert(cls: Class<*>) = cls == ObjectModifier::class.java

    override fun fromJson(jv: JsonValue) = objectModifiers.singleOrNull { it.editorID == jv.string }

    override fun toJson(value: Any) = "\"${(value as ObjectModifier).editorID}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class PerkConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == Perk::class.java

    override fun fromJson(jv: JsonValue) = Perk.get(jv.string!!)

    override fun toJson(value: Any) = "\"${(value as Perk).editorID}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class ModelConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == Model::class.java

    override fun fromJson(jv: JsonValue) = Model.get(jv.string!!)

    override fun toJson(value: Any) = "\"${(value as Model).model}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class ComponentConverter(val components: List<Component>) : Converter {
    override fun canConvert(cls: Class<*>) = cls == Component::class.java

    override fun fromJson(jv: JsonValue) = components.singleOrNull { it.editorID == jv.string!! }

    override fun toJson(value: Any) = "\"${(value as Component).editorID}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
}

class WeaponConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == Weapon::class.java

    override fun fromJson(jv: JsonValue) = com.fwdekker.fallout.weaponmods.wiki.Weapon.get(jv.string!!)

    override fun toJson(value: Any) = "\"${(value as com.fwdekker.fallout.weaponmods.wiki.Weapon).keyword}\""


    @Target(AnnotationTarget.FIELD)
    annotation class Annotation
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
    @ESMConverter.Annotation
    val file: ESM,
    @FormIDConverter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String
)

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
    @ESMConverter.Annotation
    val file: ESM,
    @FormIDConverter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String,
    val value: Int,
    val weight: Double,
    @ModelConverter.Annotation
    val model: Model
)

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
    @ESMConverter.Annotation
    val file: ESM,
    @FormIDConverter.Annotation
    val formID: FormID,
    val editorID: String,
    val name: String,
    val description: String,
    @LooseModConverter.Annotation
    val looseMod: LooseMod,
    @WeaponConverter.Annotation
    val weapon: com.fwdekker.fallout.weaponmods.wiki.Weapon,
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
    @ESMConverter.Annotation
    val file: ESM,
    @FormIDConverter.Annotation
    val formID: FormID,
    val editorID: String,
    @ObjectModifierConverter.Annotation
    val createdMod: ObjectModifier,
    val components: List<Component>,
    val conditions: List<Condition>
) {
    /**
     * A component that is required to craft the object.
     *
     * @property component the editor ID of the component required to craft the object
     * @property count the amount of the component required to craft the object
     */
    data class Component(
        @ComponentConverter.Annotation
        val component: com.fwdekker.fallout.weaponmods.xedit.Component,
        val count: Int
    )

    /**
     * Indicates a [Perk] and its corresponding rank that are required to craft the weapon mod.
     *
     * @property perk the perk
     * @property rank the rank of the perk
     */
    data class Condition(
        @PerkConverter.Annotation
        val perk: Perk,
        val rank: Int
    )
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
    @ESMConverter.Annotation
    val file: ESM,
    @FormIDConverter.Annotation
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
)
