package com.fwdekker.fallout.weaponmods


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
    "Crafting table",
    listOf<Pair<String, String>>() +
        Pair("type", type) +
        formatMaterials(materials) +
        Pair("workspace", workspace) +
        formatPerks(perks) +
        formatProducts(products)
) {
    companion object {
        private fun formatMaterials(materials: List<Pair<String, Int>>): List<Pair<String, String>> {
            return materials
                .sortedBy { it.first }
                .flatMapIndexed { i, material ->
                    listOf(
                        Pair("material${i + 1}", material.first),
                        Pair("material#${i + 1}", material.second.toString())
                    )
                }
        }

        private fun formatPerks(perks: List<Pair<String, Int>>): List<Pair<String, String>> {
            return perks
                .sortedBy { it.first }
                .mapIndexed { i, pair -> "perk${i + 1}" to "${pair.first} (${pair.second})" }
        }

        private fun formatProducts(products: List<Pair<String, Int>>): List<Pair<String, String>> {
            return products
                .sortedBy { it.first }
                .flatMapIndexed { i, product ->
                    listOf(
                        Pair("product${i + 1}", product.first),
                        Pair("product#${i + 1}", product.second.toString())
                    )
                }
        }


        private fun <T, R> Iterable<T>.flatMapIndexed(transform: (index: Int, T) -> Iterable<R>): List<R> {
            return mapIndexed { index, t -> Pair(index, t) }
                .flatMap { indexedT -> transform(indexedT.first, indexedT.second) }
        }
    }
}

// TODO document this
class WeaponModEffectTable(val weaponMods: List<WeaponMod>) {
    class WeaponModEffectHeader : WikiTemplate("FDekker/TemplateSandbox", listOf("1" to "start"))

    class WeaponModEffectRow(weaponMod: WeaponMod) : WikiTemplate(
        "FDekker/TemplateSandbox",
        listOf(
            "1" to "row",
            "weapon" to weaponMod.effects.weapon!!.wikiLink!!.text.capitalize(),
            "desc" to weaponMod.effects.description,
            "prefix" to weaponMod.effects.name,
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
