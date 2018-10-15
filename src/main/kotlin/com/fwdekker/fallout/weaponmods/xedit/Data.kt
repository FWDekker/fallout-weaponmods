package com.fwdekker.fallout.weaponmods.xedit

import com.beust.klaxon.Klaxon
import mu.KLogging
import java.io.File


// TODO document this
data class GameDatabase(
    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>,
    val weapons: List<Weapon>
) {
    companion object : KLogging() {
        fun fromDirectory(directory: File): GameDatabase? {
            val looseMods = parseFile<LooseMod>(File(directory, "misc.json"))
                ?: return null
            val objectModifiers = parseFile<ObjectModifier>(File(directory, "omod.json"))
                ?: return null
            val craftableObjects = parseFile<CraftableObject>(File(directory, "cobj.json"))
                ?: return null
            val components = parseFile<Component>(File(directory, "cmpo.json"))
                ?: return null
            val weapons = parseFile<Weapon>(File(directory, "weap.json"))
                ?: return null

            return GameDatabase(looseMods, objectModifiers, craftableObjects, components, weapons)
        }

        private inline fun <reified T> parseFile(file: File) = Klaxon().parseArray<T>(file.inputStream())
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
    val file: String,
    val formID: String,
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
    val file: String,
    val formID: String,
    val editorID: String,
    val name: String,
    val value: Int,
    val weight: Double,
    val model: String
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
 * @property weaponName the keyword of the weapon to which the effects can be applied
 */
data class ObjectModifier(
    val file: String,
    val formID: String,
    val editorID: String,
    val name: String,
    val description: String,
    val looseMod: String,
    val weaponName: String,
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
    val file: String,
    val formID: String,
    val editorID: String,
    val createdMod: String,
    val components: List<Component>,
    val conditions: List<Condition>
) {
    /**
     * A component that is required to craft the object.
     *
     * @property components the component required to craft the object
     * @property count the amount of the component required to craft the object
     */
    data class Component(
        val component: String,
        val count: Int
    )

    /**
     * A requirement for crafting a [CraftableObject].
     *
     * A requirement is expressed as the comparison between two values. On one side is the [function] to which some
     * argument is given as a parameter, and on the other side is the [comparison]—the expected value. The expected
     * relation between the two values is expressed by the [type].
     *
     * The value that is given to the [function] is either the [perk] or the [keyword]—it is not possible for both
     * values to be present in one condition.
     *
     * @property function the function on the left-hand side
     * @property perk the perk that is given to [function]
     * @property keyword the keyword that is given to [function]
     * @property type the type of comparison (e.g. "Equal to")
     * @property comparison the expected value on the right-hand side
     */
    data class Condition(
        val function: String,
        val perk: String,
        val keyword: String,
        val type: String,
        val comparison: String
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
    // TODO rename this without conflicting with the wiki weapon
    val file: String,
    val formID: String,
    val editorID: String,
    val name: String,
    val speed: Float,
    val reloadSpeed: Float,
    val reach: Float,
    val minRange: Float,
    val maxRange: Float,
    val attackDelay: Float,
    val weight: Float,
    val value: Int,
    val baseDamage: Int
)