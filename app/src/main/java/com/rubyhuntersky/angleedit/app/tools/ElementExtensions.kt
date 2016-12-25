package com.rubyhuntersky.angleedit.app.tools

import android.content.Intent
import android.net.Uri
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.Text

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

val Element.firstTextString: String? get() = textNodes().firstOrNull()?.textContent?.trim()
val Element.toHttpUri: Uri? get() = firstTextString?.toHttpUri
val Element.toViewIntent: Intent? get() = toHttpUri?.toViewIntent

val Element.toTitleText: String get() = if (textContent.trim().isNullOrEmpty()) {
    tagName
} else {
    textContent
}

val Element.attributeMap: Map<String, String> get() {
    val map = mutableMapOf<String, String>()
    attributes.items.forEach { map.put(it.nodeName, it.textContent) }
    return map
}

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

fun Element.flatten(): List<Element> {
    val list = mutableListOf<Element>()
    this.addToList(list)
    return list
}

fun Element.findDescendantWithTagList(tagList: List<String>?): Element? {
    tagList ?: return null
    return flatten().find { it.asTagList == tagList }
}

fun Element.addToList(list: MutableList<Element>) {
    list.add(this)
    this.elementNodes.forEach { it.addToList(list) }
}

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

val Uri.toViewIntent: Intent get() = Intent(Intent.ACTION_VIEW, this)

val Node.elementNodes: List<Element> get() = (0 until childNodes.length)
        .map { childNodes.item(it) }
        .filter { it.nodeType == Node.ELEMENT_NODE }
        .map { it as Element }

val NamedNodeMap.items: List<Node> get() = (0 until length).map { item(it) }

