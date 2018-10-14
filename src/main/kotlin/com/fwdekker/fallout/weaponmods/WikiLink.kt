package com.fwdekker.fallout.weaponmods


open class Link(val target: String, val text: String = target) {
    override fun toString() =
        if (target.capitalize() == text.capitalize())
            "[[$target]]"
        else
            "[[$target|$text]]"
}

class Category(category: String) : Link("Category:$category")

class InterlanguageLink(language: String, page: String) : Link("$language:$page")
