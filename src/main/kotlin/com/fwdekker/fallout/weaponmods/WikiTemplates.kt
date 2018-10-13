package com.fwdekker.fallout.weaponmods


fun formIDtoTemplate(formID: String) =
    if (formID.dropWhile { it == '0' }.length > 6)
        "{{DLC ID|${formID.takeLast(6)}}}"
    else
        "{{ID|${formID.takeLast(6)}}}"

open class Link(val target: String, val text: String?) {
    override fun toString() =
        if (text == null)
            "[[$target]]"
        else
            "[[$target|$text]]"
}

data class Infobox(
    val type: String,
    val values: List<Pair<String, String>>,
    val keyWidth: Int = (values.map { it.first.length }.max() ?: 0) + 1
) {
    override fun toString() = "" +
        "{{$type" +
        values.map{ "|" + it.first.padEnd(keyWidth) + "=" + it.second + "\n" } +
        "}}"
}

class Category(category: String) : Link("Category:$category", null)

class InterlanguageLink(language: String, page: String) : Link("$language:$page", null)

open class Page {
    val notices = mutableListOf<String>()
    val infoboxes = mutableListOf<Infobox>()
    val games = mutableListOf<String>()
    var intro = mutableListOf<String>()
    val sections = mutableListOf<Pair<String, String>>()
    val navboxes = mutableListOf<String>()
    val categories = mutableListOf<Link>()
    val interlanguageLinks = mutableListOf<InterlanguageLink>()

    override fun toString(): String = "" +
        notices.joinElements { "{{$it}}" } +
        infoboxes.joinElements() +
        "{{Games|${games.joinToString("|")}}}" +
        "\n" +
        intro.joinSections() +
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
