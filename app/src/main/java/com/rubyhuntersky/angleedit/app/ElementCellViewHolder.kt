package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.cell_attribute.view.*
import kotlinx.android.synthetic.main.cell_element.view.*
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

class ElementCellViewHolder(val itemView: View) {
    constructor(context: Context) : this(LayoutInflater.from(context).inflate(R.layout.cell_element, null, false))

    fun bind(element: Element) {
        val detailText = element.firstTextString ?: ""
        if (detailText.isNotEmpty()) {
            itemView.textView.text = detailText
            itemView.secondaryTextView.text = element.tagName
            itemView.secondaryTextView.visibility = View.VISIBLE
            itemView.chipView.visibility = View.GONE
        } else if (element.attributes.length > 0) {
            itemView.textView.text = "${element.tagName}\u2003"
            itemView.secondaryTextView.visibility = View.GONE
            itemView.chipView.text = element.attributes.item(0).textContent
            itemView.chipView.visibility = View.VISIBLE
        } else {
            itemView.textView.text = element.tagName
            itemView.secondaryTextView.visibility = View.GONE
            itemView.chipView.visibility = View.GONE
        }
    }

    private fun getDetailText(element: Element): String? {
        val attributes = element.attributes
        return if (attributes.length > 0) {
            joinStrings(getAttributeDisplayStrings(attributes))
        } else {
            element.firstTextString
        }
    }

    private fun joinStrings(strings: List<String>): String {
        val builder = StringBuilder()
        for (string in strings) {
            if (builder.isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(string)
        }
        return builder.toString()
    }

    private fun getAttributeDisplayStrings(attributes: NamedNodeMap): List<String> {
        return (0..attributes.length - 1).map {
            getAttributeDisplayString(attributes.item(it))
        }
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