package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.rubyhuntersky.angleedit.app.data.AccentCenter
import com.rubyhuntersky.angleedit.app.tools.asTagList
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
    private fun getColor(colorId: Int): Int = ContextCompat.getColor(itemView.context, colorId)
    private fun getPrimaryColorId(accented: Boolean): Int = if (accented) R.color.tagname_body1_accented else R.color.tagname_body1
    private fun getSecondaryColorId(accented: Boolean): Int = if (accented) R.color.tagname_secondary_accented else R.color.tagname_secondary

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
        val isAccented = AccentCenter.containsAccent(element.asTagList)
        if (detailText.isNotEmpty()) {
            bindForText(truncateForDisplay(detailText), element, isAccented)
        } else if (element.attributes.length > 0) {
            bindForAttributes(element, isAccented)
        } else {
            bindForTag(element, isAccented)
        }
    }

    private fun bindForTag(element: Element, accented: Boolean) {
        itemView.tagTextView.text = element.tagName
        itemView.tagTextView.setTextColor(getColor(getPrimaryColorId(accented)))
        itemView.tagTextView.visibility = View.VISIBLE

        itemView.contentTextView.visibility = View.GONE
        itemView.secondaryTextView.visibility = View.GONE
        itemView.chipsLayout.visibility = View.GONE
    }

    private fun bindForText(detailText: String, element: Element, accented: Boolean) {
        itemView.contentTextView.text = detailText
        itemView.contentTextView.setTextColor(getColor(getPrimaryColorId(accented)))
        itemView.contentTextView.visibility = View.VISIBLE
        itemView.secondaryTextView.text = element.tagName
        itemView.secondaryTextView.setTextColor(getColor(getSecondaryColorId(false)))
        itemView.secondaryTextView.visibility = View.VISIBLE

        itemView.tagTextView.visibility = View.GONE
        itemView.chipsLayout.visibility = View.GONE
    }

    private fun bindForAttributes(element: Element, accented: Boolean) {
        val tagTextColor = getColor(getPrimaryColorId(accented))
        val chipTextColor = getColor(getSecondaryColorId(accented))
        itemView.tagTextView.text = "${element.tagName}\u2003"
        itemView.tagTextView.setTextColor(tagTextColor)
        itemView.tagTextView.visibility = View.VISIBLE
        val allItems = element.attributes.items
        val displayItems = if (allItems.size < 5) allItems else allItems.subList(0, 5)
        if (itemView.chipsLayout.childCount == displayItems.size) {
            displayItems.forEachIndexed { i, node ->
                val chipView = itemView.chipsLayout.getChildAt(i) as TextView
                chipView.text = node.textContent
                chipView.setTextColor(chipTextColor)
            }
        } else {
            itemView.chipsLayout.removeAllViews()
            displayItems.forEach {
                val chipView = it.toUnattachedChipView(itemView.chipsLayout)
                chipView.setTextColor(chipTextColor)
                val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                layoutParams.marginEnd = chipMargin
                layoutParams.marginStart = chipMargin
                layoutParams.gravity = Gravity.CENTER_VERTICAL
                itemView.chipsLayout.addView(chipView, layoutParams)
            }
        }
        itemView.chipsLayout.visibility = View.VISIBLE

        itemView.contentTextView.visibility = View.GONE
        itemView.secondaryTextView.visibility = View.GONE
    }

    private fun Node.toUnattachedChipView(parent: ViewGroup): TextView {
        val chipView = layoutInflater.inflate(R.layout.cell_attribute, parent, false) as TextView
        chipView.text = textContent
        return chipView
    }
}