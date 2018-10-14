package com.fwdekker.fallout.weaponmods


fun formIDtoTemplate(formID: String) =
    if (formID.dropWhile { it == '0' }.length > 6)
        "{{DLC ID|${formID.takeLast(6)}}}"
    else
        "{{ID|${formID.takeLast(6)}}}"

open class MultilineTemplate(
    private val template: String,
    private val values: List<Pair<String, String>>,
    private val keyWidth: Int = (values.map { it.first.length }.max() ?: 0) + 1
) {
    override fun toString() = "" +
        "{{$template\n" +
        values.joinToString("\n") { "|" + it.first.padEnd(keyWidth) + "=" + it.second } + "\n" +
        "}}"
}

data class CraftingTable(
    val type: String = "",
    val materials: List<Pair<String, Int>>,
    val workspace: String,
    val perks: List<Pair<String, Int>>,
    val products: List<Pair<String, Int>>
) : MultilineTemplate(
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
        perks.sortedBy { it.first }.mapIndexed { i, pair -> "perk$i" to "${pair.first} (${pair.second})" } +
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
