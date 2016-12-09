package com.rubyhuntersky.angleedit.app.tools

import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_recent_sources.view.*

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

class RecentSourcesViewHolder(val view: View) {
    fun bind(sourceStrings: List<String>, onClick: (String) -> Unit) {
        val sourceViews: List<TextView> = listOf(view.recentSource1 as TextView, view.recentSource2 as TextView, view.recentSource3 as TextView)
        sourceViews.forEachIndexed { i, view ->
            if (i < sourceStrings.size) {
                view.text = sourceStrings[i]
                view.setOnClickListener {
                    onClick(sourceStrings[i])
                }
            } else {
                view.text = ""
                view.setOnClickListener(null)
            }
        }
    }
}