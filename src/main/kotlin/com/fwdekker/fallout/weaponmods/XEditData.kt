package com.fwdekker.fallout.weaponmods


/**
 * A component. (CMPO)
 *
 * @property file the ESM in which the recipe is defined
 * @property formID the base ID of the recipe as a decimal number
 * @property editorID the editor ID of the recipe
 * @property name the name of the component
 */
data class Component(
    val file: String,
    val formID: Int,
    val editorID: String,
    val name: String
)

/**
 * The weapon mod as seen in the player character's inventory. (MISC)
 *
 * @property file the ESM in which the recipe is defined
 * @property formID the base ID of the recipe as a decimal number
 * @property editorID the editor ID of the recipe
 * @property name the name of the weapon mod
 * @property value the value of the weapon mod in bottle caps
 * @property weight the weight of the weapon mod in pounds
 * @property model the path to the in-game model file for the weapon mod
 */
data class LooseMod(
    val file: String,
    val formID: Int,
    val editorID: String,
    val name: String,
    val value: Int,
    val weight: Double,
    val model: String
)

/**
 * The effects of the weapon mod. (OMOD)
 *
 * @property file the ESM in which the recipe is defined
 * @property formID the base ID of the recipe as a decimal number
 * @property editorID the editor ID of the recipe
 * @property name the name of the weapon mod
 * @property description the effects of the weapon mod
 * @property looseMod the editor ID of the corresponding [LooseMod]
 * @property weaponName the keyword of the weapon to which the effects can be applied
 */
data class ObjectModifier(
    val file: String,
    val formID: Int,
    val editorID: String,
    val name: String,
    val description: String,
    val looseMod: String,
    val weaponName: String
)

/**
 * The recipe for the weapon mod. (COBJ)
 *
 * @property file the ESM in which the recipe is defined
 * @property formID the base ID of the recipe as a decimal number
 * @property editorID the editor ID of the recipe
 * @property createdMod the editor ID of the [ObjectModifier] that is created by this recipe
 * @property components the components required to use this recipe
 * @property conditions the requirements (e.g. perks) to use this recipe
 */
data class CraftableObject(
    val file: String,
    val formID: Int,
    val editorID: String,
    val createdMod: String,
    val components: List<CraftableObject.Component>,
    val conditions: List<CraftableObject.Condition>
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
