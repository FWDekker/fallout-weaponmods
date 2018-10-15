package com.fwdekker.fallout.weaponmods.wiki

import com.fwdekker.fallout.weaponmods.WeaponMod


/**
 * A MediaWiki template.
 *
 * @property template the name of the template
 * @property values the key-value pairs of the template
 */
open class WikiTemplate(
    private val template: String,
    private val values: List<Pair<String, String>> = emptyList()
) {
    /**
     * Formats the template as a multiline string.
     *
     * @return the template as a multiline string
     */
    override fun toString(): String {
        return toString(true)
    }

    /**
     * Formats the template as a string.
     *
     * @param multiline true iff each key-value pair should be on its own line
     * @return the template as a string
     */
    fun toString(multiline: Boolean): String {
        val keyWidth = (values.map { it.first.length }.max() ?: 0) + 1

        // TODO remove duplication
        return if (multiline)
            "" +
                "{{$template\n" +
                values.joinToString("\n") {
                    if (it.first.toIntOrNull() != null)
                        "|${it.second}"
                    else
                        "|${it.first.padEnd(keyWidth)}=${it.second}"
                } + "\n" +
                "}}"
        else
            "" +
                "{{$template" +
                values.joinToString("") {
                    if (it.first.toIntOrNull() != null)
                        "|${it.second}"
                    else
                        "|${it.first}=${it.second}"
                } +
                "}}"
    }
}

// TODO document this
class CraftingTable(
    type: String = "",
    materials: List<Pair<String, Int>>,
    workspace: String,
    perks: List<Pair<String, Int>>,
    products: List<Pair<String, Int>>
) : WikiTemplate(
    // TODO clean up these parameters
    "Crafting table",
    listOf<Pair<String, String>>() +
        listOf("type" to type) +
        materials
            .sortedBy { it.first }
            .mapIndexed { i, b -> Pair(i + 1, b) }
            .flatMap { b ->
                listOf(
                    Pair("material${b.first}", b.second.first),
                    Pair("material#${b.first}", b.second.second.toString())
                )
            } +
        Pair("workspace", workspace) +
        perks.sortedBy { it.first }.mapIndexed { i, pair -> "perk${i + 1}" to "${pair.first} (${pair.second})" } +
        products
            .sortedBy { it.first }
            .mapIndexed { i, b -> Pair(i + 1, b) }
            .flatMap { b ->
                listOf(
                    Pair("product${b.first}", b.second.first),
                    Pair("product#${b.first}", b.second.second.toString())
                )
            }
) {
    override fun toString() = super.toString() // This is necessary for some reason
}

// TODO document this
class WeaponModEffectTable(val weaponMods: List<WeaponMod>) {
    class WeaponModEffectHeader : WikiTemplate("FDekker/TemplateSandbox", listOf("1" to "start"))

    class WeaponModEffectRow(weaponMod: WeaponMod) : WikiTemplate(
        "FDekker/TemplateSandbox",
        listOf(
            "1" to "row",
            "weapon" to weaponMod.weapon.name.capitalize(),
            "desc" to weaponMod.description,
            "prefix" to weaponMod.prefix,
            "damage" to "+1",
            "attack" to "+2",
            "range" to "+3",
            "spread" to "+4",
            "magazine" to "+5",
            "weight" to "+6",
            "value" to "+7"
        )
    )

    class WeaponModEffectFooter : WikiTemplate("FDekker/TemplateSandbox", listOf("1" to "end"))


    override fun toString(): String {
        return "" +
            WeaponModEffectHeader().toString(multiline = false) + "\n" +
            weaponMods.joinToString("\n") {
                WeaponModEffectRow(
                    it).toString(multiline = false)
            } + "\n" +
            WeaponModEffectFooter().toString(multiline = false)
    }
}
