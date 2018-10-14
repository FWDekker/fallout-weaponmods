package com.fwdekker.fallout.weaponmods


/**
 * An article on Nukapedia.
 *
 * @property notices the notices that are put at the top of the article
 * @property infoboxes the infoboxes on the article
 * @property games a list of the games the article corresponds to
 * @property intro the leading text
 * @property sections the sections on the article
 * @property navboxes the navboxes at the bottom of the article
 * @property categories the categories the article belongs to
 * @property interlanguageLinks the translations of this article
 */
class Article {
    val notices = mutableListOf<String>()
    val infoboxes = mutableListOf<WikiTemplate>()
    val games = mutableListOf<String>()
    var intro: String = ""
    val sections = mutableListOf<Section>()
    val navboxes = mutableListOf<WikiTemplate>()
    val categories = mutableListOf<Link>()
    val interlanguageLinks = mutableListOf<InterlanguageLink>()


    /**
     * Returns a version of this string with a newline appended to it if this string is not empty.
     *
     * @return a version of this string with a newline appended to it if this string is not empty
     */
    private fun String.addNewlineIfNotEmpty(): String {
        return addNewlinesIfNotEmpty(1)
    }

    /**
     * Returns a version of this string with the given number of newlines appended to it if this string is not empty.
     *
     * @param newlines the number of newline to append of this string is not empty
     * @return a version of this string with the given number of newlines appended to it if this string is not empty
     */
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
        return navboxes.joinToString("\n") { it.toString(multiline = false) }.addNewlinesIfNotEmpty(2)
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


    /**
     * Formats the article as a string.
     *
     * @return the article as a string
     */
    override fun toString(): String {
        return formatHeader() + formatBody() + formatFooter()
    }
}

/**
 * Represents a section on a Fallout Wiki article.
 *
 * @property title the name of the section
 * @property contents the contents of the section, excluding the contents of subsections
 * @property level the number of `=` to surround the section's header with
 * @property subsections a list of subsections in this section
 */
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


    /**
     * Returns the section as a string.
     *
     * @return the section as a string
     */
    override fun toString(): String {
        return "" +
            formatTitle() +
            contents +
            formatSubsections()
    }
}
