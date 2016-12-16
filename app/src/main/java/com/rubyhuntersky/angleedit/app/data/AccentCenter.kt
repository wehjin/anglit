package com.rubyhuntersky.angleedit.app.data

import org.w3c.dom.Element

/**
 * @author Jeffrey Yu
 * @since 12/15/16.
 */

val Element.asTagList: List<String>
    get() {
        val tagList = mutableListOf<String>()
        var todo: Element? = this
        while (todo != null) {
            tagList.add(todo.tagName)
            todo = todo.parentNode as? Element
        }
        return tagList.toList()
    }

object AccentCenter {

    private val accents = mutableSetOf(
            listOf("title", "channel", "rss"),
            listOf("title", "item", "channel", "rss")
    )


    fun containsAccent(element: Element): Boolean {
        return accents.contains(element.asTagList)
    }

    fun removeAccent(element: Element?) {
        element ?: return
        accents.remove(element.asTagList)
    }

    fun addAccent(element: Element) {
        accents.add(element.asTagList)
    }
}

