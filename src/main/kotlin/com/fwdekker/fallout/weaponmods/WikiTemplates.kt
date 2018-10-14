package com.fwdekker.fallout.weaponmods


fun formIDtoTemplate(formID: String): String {
    return if (formID.dropWhile { it == '0' }.length > 6)
        "{{DLC ID|${formID.takeLast(6)}}}"
    else
        "{{ID|${formID.takeLast(6)}}}"
}

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
    // TODO support unnamed arguments
    private val keyWidth: Int = (values.map { it.first.length }.max() ?: 0) + 1


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
        val newline = if (multiline) "\n" else ""

        return "" +
            "{{$template$newline" +
            values.joinToString(newline) { "|" + it.first.padEnd(keyWidth) + "=" + it.second } + newline +
            "}}"
    }
}

data class CraftingTable(
    val type: String = "",
    val materials: List<Pair<String, Int>>,
    val workspace: String,
    val perks: List<Pair<String, Int>>,
    val products: List<Pair<String, Int>>
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
