package com.fwdekker.fallout.weaponmods.wiki


/**
 * A MediaWiki link.
 *
 * @property target the name of the page the link goes to
 * @property text the text that appears in the link
 */
open class Link(val target: String, val text: String = target) {
    /**
     * Returns a string representation of the link.
     *
     * If [target] and [text] are equivalent, the link is simplified.
     *
     * @return a string representation of the link
     */
    override fun toString(): String {
        return toString(false)
    }

    /**
     * Returns a string representation of the link.
     *
     * If [target] and [text] are equivalent, the link is simplified.
     *
     * @param capitalize true iff the [text] part of the link should be capitalized
     * @return a string representation of the link
     */
    fun toString(capitalize: Boolean): String {
        return if (target.capitalize() == text.capitalize())
            "[[${text.capitalize(capitalize)}]]"
        else
            "[[$target|${text.capitalize(capitalize)}]]"
    }


    /**
     * Capitalizes this string unless [capitalize] is set to false.
     *
     * @param capitalize true iff this string should be capitalized
     * @return the capitalized version of this string unless [capitalize] is set to false
     */
    private fun String.capitalize(capitalize: Boolean): String {
        return if (capitalize)
            this.capitalize()
        else
            this
    }
}

/**
 * Indicates that a page belongs to a certain category.
 *
 * @param category the name of the category
 */
class Category(category: String) : Link("Category:$category")

/**
 * Indicates that the page is available in another language.
 *
 * @param language the other language the page is available in
 * @param page the name of the page in the other language
 */
class InterlanguageLink(language: String, page: String) : Link("$language:$page")
