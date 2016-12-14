package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.rubyhuntersky.angleedit.app.tools.firstTextString
import com.rubyhuntersky.angleedit.app.tools.items
import kotlinx.android.synthetic.main.cell_element.view.*
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

class ElementCellViewHolder(val itemView: View) {
    constructor(context: Context) : this(LayoutInflater.from(context).inflate(R.layout.cell_element, null, false))

    private val layoutInflater: LayoutInflater get() = LayoutInflater.from(itemView.context)
    private val resources: Resources get() = itemView.context.resources
    private val chipMargin: Int get() = resources.getDimensionPixelSize(R.dimen.chip_margin)
    val maxTextChars = 120

    fun truncateForDisplay(string: String): String {
        return if (string.length > maxTextChars) {
            string.substring(0, maxTextChars)
        } else {
            string
        }
    }

    fun bind(element: Element) {
        val detailText = element.firstTextString ?: ""
        if (detailText.isNotEmpty()) {
            bindForText(truncateForDisplay(detailText), element)
        } else if (element.attributes.length > 0) {
            bindForAttributes(element)
        } else {
            bindForTag(element)
        }
    }

    private fun bindForTag(element: Element) {
        val tagName = element.tagName
        itemView.tagTextView.text = tagName
        itemView.tagTextView.visibility = View.VISIBLE
        itemView.contentTextView.visibility = View.GONE
        itemView.secondaryTextView.visibility = View.GONE
        itemView.chipsLayout.visibility = View.GONE
    }

    private fun bindForText(detailText: String, element: Element) {
        itemView.contentTextView.text = detailText
        itemView.contentTextView.visibility = View.VISIBLE
        itemView.tagTextView.visibility = View.GONE
        itemView.secondaryTextView.text = element.tagName
        itemView.secondaryTextView.visibility = View.VISIBLE
        itemView.chipsLayout.visibility = View.GONE
    }

    private fun bindForAttributes(element: Element) {
        itemView.tagTextView.text = "${element.tagName}\u2003"
        itemView.tagTextView.visibility = View.VISIBLE
        itemView.contentTextView.visibility = View.GONE
        itemView.secondaryTextView.visibility = View.GONE
        itemView.chipsLayout.visibility = View.VISIBLE
        val allItems = element.attributes.items
        val displayItems = if (allItems.size < 5) allItems else allItems.subList(0, 5)
        if (itemView.chipsLayout.childCount == displayItems.size) {
            displayItems.forEachIndexed { i, node ->
                val chipView = itemView.chipsLayout.getChildAt(i) as TextView
                chipView.text = node.textContent
            }
        } else {
            itemView.chipsLayout.removeAllViews()
            displayItems.forEach {
                val chipView = it.toUnattachedChipView(itemView.chipsLayout)
                val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                layoutParams.marginEnd = chipMargin
                layoutParams.marginStart = chipMargin
                layoutParams.gravity = Gravity.CENTER_VERTICAL
                itemView.chipsLayout.addView(chipView, layoutParams)
            }
        }
    }

    private fun Node.toUnattachedChipView(parent: ViewGroup): TextView {
        val chipView = layoutInflater.inflate(R.layout.cell_attribute, parent, false) as TextView
        chipView.text = textContent
        return chipView
    }
}