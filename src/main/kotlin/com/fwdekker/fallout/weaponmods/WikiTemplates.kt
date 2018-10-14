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
    val materials: List<Pair<String, Range>>,
    val workspace: String,
    val perks: List<Pair<String, Range>>,
    val products: List<Pair<String, Range>>
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

open class Page {
    val notices = mutableListOf<String>()
    val infoboxes = mutableListOf<MultilineTemplate>()
    val games = mutableListOf<String>()
    val intros = mutableListOf<String>()
    val sections = mutableListOf<Pair<String, String>>()
    val navboxes = mutableListOf<String>()
    val categories = mutableListOf<Link>()
    val interlanguageLinks = mutableListOf<InterlanguageLink>()

    override fun toString(): String = "" +
        notices.joinElements { "{{$it}}" } +
        infoboxes.joinToString("\n") +
        "{{Games|${games.joinToString("|")}}}\n" +
        "\n" +
        intros.joinSections() +
        sections.joinSections { "==${it.first}==\n${it.second}" } +
        navboxes.joinSections { "{{$it}}" } +
        categories.joinElementSection() +
        interlanguageLinks.joinElementSection()
}


fun <T> List<T>.join(separator: CharSequence, maybePostfix: CharSequence, transform: ((T) -> CharSequence)? = null) =
    if (this.isEmpty())
        ""
    else
        this.joinToString(separator, transform = transform) + maybePostfix

fun <T> List<T>.joinElements(transform: ((T) -> CharSequence)? = null) = this.join("\n", "\n", transform)

fun <T> List<T>.joinElementSection(transform: ((T) -> CharSequence)? = null) = this.join("\n", "\n\n", transform)

fun <T> List<T>.joinSections(transform: ((T) -> CharSequence)? = null) = this.join("\n\n", "\n\n", transform)
