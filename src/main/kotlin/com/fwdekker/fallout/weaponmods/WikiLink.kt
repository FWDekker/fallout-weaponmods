package com.fwdekker.fallout.weaponmods


open class Link(val target: String, val text: String?) {
    override fun toString() =
        if (text == null)
            "[[$target]]"
        else
            "[[$target|$text]]"
}

class Category(category: String) : Link("Category:$category", null)

class InterlanguageLink(language: String, page: String) : Link("$language:$page", null)
