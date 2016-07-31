package com.rubyhuntersky.angleedit.app

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

fun Element.nodes(): List<Node> {
    val childNodes = this.childNodes
    if (childNodes.length == 0) {
        return emptyList()
    }
    val nodes = mutableListOf<Node>()
    for (i in 0..(childNodes.length - 1)) {
        nodes.add(childNodes.item(i))
    }
    return nodes
}

fun Element.textNodes(): List<Text> {
    return this.nodes().filter { it is Text && it.textContent.isNotEmpty() }.map { it as Text }
}

val Element.firstTextString: String? get() {
    return textNodes().firstOrNull()?.textContent?.trim() ?: null
}
