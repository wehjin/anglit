package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import java.util.*

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

class ElementCellViewHolder(val itemView: View) {
    constructor(context: Context) : this(LayoutInflater.from(context).inflate(R.layout.cell_element, null, false))

    val primaryTextView: TextView = itemView.findViewById(R.id.textView) as TextView
    val secondaryTextView: TextView = itemView.findViewById(R.id.secondaryTextView) as TextView


    fun bind(element: Element) {
        val detailText = getDetailText(element)
        if (detailText.isNullOrEmpty()) {
            primaryTextView.text = element.tagName
            secondaryTextView.visibility = View.GONE
        } else {
            primaryTextView.text = detailText
            secondaryTextView.text = element.tagName
        }
    }

    private fun getDetailText(element: Element): String? {
        val attributes = element.attributes
        if (attributes.length > 0) {
            return joinStrings(getAttributeDisplayStrings(attributes))
        }

        val textNode = element.textNodes().firstOrNull()
        if (textNode != null) {
            val trim = textNode.textContent.trim()
            Log.d(ElementCellViewHolder::class.java.simpleName, "Detail: $trim")
            return trim
        }

        return null
    }

    private fun joinStrings(strings: List<String>): String {
        val builder = StringBuilder()
        for (string in strings) {
            if (builder.length > 0) {
                builder.append(", ")
            }
            builder.append(string)
        }
        return builder.toString()
    }

    private fun getAttributeDisplayStrings(attributes: NamedNodeMap): List<String> {
        val displayStrings = ArrayList<String>()
        for (index in 0..attributes.length - 1) {
            displayStrings.add(getAttributeDisplayString(attributes.item(index)))
        }
        return displayStrings
    }

    private fun getAttributeDisplayString(attributeNode: Node): String {
        val nodeValue = attributeNode.nodeValue
        if (nodeValue == null || nodeValue == "") {
            return attributeNode.nodeName
        } else {
            return nodeValue.trim { it <= ' ' }
        }
    }
}