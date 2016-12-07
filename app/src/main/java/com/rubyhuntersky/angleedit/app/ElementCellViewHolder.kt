package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.rubyhuntersky.angleedit.app.tools.firstTextString
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

    private val NamedNodeMap.items: List<Node> get() = (0 until length).map { item(it) }
    private val layoutInflater: LayoutInflater get() = LayoutInflater.from(itemView.context)
    private val resources: Resources get() = itemView.context.resources
    private val chipMargin: Int get() = resources.getDimensionPixelSize(R.dimen.chip_margin)
    private fun Node.toUnattachedChipView(parent: ViewGroup): TextView {
        val chipView = layoutInflater.inflate(R.layout.cell_attribute, parent, false) as TextView
        chipView.text = textContent
        return chipView
    }

    fun bind(element: Element) {
        val detailText = element.firstTextString ?: ""
        if (detailText.isNotEmpty()) {
            itemView.textView.text = detailText
            itemView.secondaryTextView.text = element.tagName
            itemView.secondaryTextView.visibility = View.VISIBLE
            itemView.chipsLayout.visibility = View.GONE
            itemView.chipView.visibility = View.GONE
        } else if (element.attributes.length > 0) {
            itemView.textView.text = "${element.tagName}\u2003"
            itemView.secondaryTextView.visibility = View.GONE
            itemView.chipsLayout.visibility = View.VISIBLE
            itemView.chipsLayout.removeAllViews()
            element.attributes.items.forEach {
                val chipView = it.toUnattachedChipView(itemView.chipsLayout)
                val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                layoutParams.marginEnd = chipMargin
                layoutParams.marginStart = chipMargin
                itemView.chipsLayout.addView(chipView, layoutParams)
            }
        } else {
            itemView.textView.text = element.tagName
            itemView.secondaryTextView.visibility = View.GONE
            itemView.chipsLayout.visibility = View.GONE
        }
    }
}