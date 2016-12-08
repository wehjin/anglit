package com.rubyhuntersky.angleedit.app.tools

import android.net.Uri
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

fun Element.nodes(): List<Node> {
    val childNodes = this.childNodes
    return if (childNodes.length == 0) {
        emptyList()
    } else {
        (0 until childNodes.length).map { childNodes.item(it) }
    }
}

fun Element.textNodes(): List<Text> = this.nodes()
        .filter { it is Text && it.textContent.isNotEmpty() }
        .map { it as Text }

val Element.firstTextString: String? get() = textNodes().firstOrNull()?.textContent?.trim()
val Element.asHttpUri: Uri? get() = firstTextString?.asHttpUri

val Node.elementNodes: List<Element> get() = (0 until childNodes.length)
        .map { childNodes.item(it) }
        .filter { it.nodeType == Node.ELEMENT_NODE }
        .map { it as Element }
