package com.fwdekker.fallout.weaponmods


fun formIDtoTemplate(formID: String) =
    if (formID.dropWhile { it == '0' }.length > 6)
        "{{DLC ID|${formID.takeLast(6)}}}"
    else
        "{{ID|${formID.takeLast(6)}}}"

class Section(
    private val title: String,
    private val contents: String,
    private val level: Int = 2,
    private val subsections: List<Section> = emptyList()
) {
    private fun formatTitle(): String {
        return "=".repeat(level) + title + "=".repeat(level) + "\n"
    }

    private fun formatSubsections(): String {
        return when {
            subsections.isEmpty() -> ""
            contents.isEmpty() -> subsections.joinToString("\n\n")
            else -> "\n\n" + subsections.joinToString("\n\n")
        }
    }


    override fun toString(): String {
        return "" +
            formatTitle() +
            contents +
            formatSubsections()
    }
}

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

class Page {
    val notices = mutableListOf<String>()
    val infoboxes = mutableListOf<MultilineTemplate>()
    val games = mutableListOf<String>()
    var intro: String = ""
    val sections = mutableListOf<Section>()
    val navboxes = mutableListOf<String>()
    val categories = mutableListOf<Link>()
    val interlanguageLinks = mutableListOf<InterlanguageLink>()


    private fun String.addNewlineIfNotEmpty(): String {
        return addNewlinesIfNotEmpty(1)
    }

    private fun String.addNewlinesIfNotEmpty(newlines: Int): String {
        return if (this.isEmpty())
            ""
        else
            this + "\n".repeat(newlines)
    }


    private fun formatNotices(): String {
        return notices.joinToString("\n")
    }

    private fun formatInfoboxes(): String {
        return infoboxes.joinToString("\n")
    }

    private fun formatGames(): String {
        return if (games.isEmpty())
            ""
        else
            "{{Games|${games.joinToString("|")}}}"
    }

    private fun formatHeader(): String {
        var result = ""
        result += formatNotices().addNewlineIfNotEmpty()
        result += formatInfoboxes()
        if (!formatGames().isEmpty())
            result += formatGames()

        if (result.isNotEmpty())
            result += "\n\n"

        return result
    }


    private fun formatIntro(): String {
        return intro.addNewlinesIfNotEmpty(2)
    }

    private fun formatSections(): String {
        return sections.joinToString("\n\n").addNewlinesIfNotEmpty(2)
    }

    private fun formatBody(): String {
        return formatIntro() + formatSections()
    }


    private fun formatNavboxes(): String {
        return navboxes.joinToString("\n").addNewlinesIfNotEmpty(2)
    }

    private fun formatCategories(): String {
        return categories.joinToString("\n").addNewlinesIfNotEmpty(2)
    }

    private fun formatInterlanguageLinks(): String {
        return interlanguageLinks.joinToString("\n")
    }

    private fun formatFooter(): String {
        return formatNavboxes() + formatCategories() + formatInterlanguageLinks()
    }


    override fun toString(): String {
        return formatHeader() + formatBody() + formatFooter()
    }
}
