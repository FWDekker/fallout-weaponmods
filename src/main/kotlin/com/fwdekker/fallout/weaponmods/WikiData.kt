package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File


fun formIDtoTemplate(formID: String) =
    if (formID.dropWhile { it == '0' }.length > 6)
        "{{DLC ID|${formID.takeLast(6)}}}"
    else
        "{{ID|${formID.takeLast(6)}}}"

data class Weapon(
    val file: String,
    val keyword: String,
    val name: String,
    val page: String
) {
    fun getWikiLink() =
        if (name.capitalize() == page) "[[${name.capitalize()}]]"
        else "[[$page|${name.capitalize()}]]"


    companion object {
        private val weapons = Klaxon().parseArray<Weapon>(File("weapons.json").inputStream())!!
            .map { Pair(it.keyword.toLowerCase(), it) }
            .toMap()

        fun get(keyword: String) = weapons[keyword.toLowerCase()]
    }
}

data class Model(
    val model: String,
    val image: String
) {
    companion object {
        private val models = Klaxon().parseArray<Model>(File("models.json").inputStream())!!
            .map { Pair(it.model.toLowerCase(), it) }
            .toMap()

        fun get(model: String) = models[model.toLowerCase()]
    }
}

open class Link(val target: String, val text: String?) {
    override fun toString() =
        if (text == null)
            "[[$target]]"
        else
            "[[$target|$text]]"
}

class Category(val category: String) : Link("Category:$category", null)

class InterlanguageLink(val language: String, val page: String) : Link("$language:$page", null)

open class Page {
    val notices = mutableListOf<String>()
    val infoboxes = mutableListOf<String>()
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
        intro.joinSections() +
        sections.joinSections { "==${it.first}==\n${it.second}" } +
        navboxes.joinSections { "{{$it}}" } +
        categories.joinElements() +
        interlanguageLinks.joinElements()
}

fun <T> List<T>.join(
    separator: CharSequence,
    maybePostfix: CharSequence,
    transform: ((T) -> CharSequence)? = null
) =
    if (this.isEmpty())
        ""
    else
        this.joinToString(separator, transform = transform) + maybePostfix

fun <T> List<T>.joinElements(transform: ((T) -> CharSequence)? = null) = this.join("\n", "\n", transform)

fun <T> List<T>.joinElementSection(transform: ((T) -> CharSequence)? = null) = this.join("\n", "\n\n", transform)

fun <T> List<T>.joinSections(transform: ((T) -> CharSequence)? = null) = this.join("\n\n", "\n\n", transform)
