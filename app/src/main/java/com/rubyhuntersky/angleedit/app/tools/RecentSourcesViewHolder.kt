package com.rubyhuntersky.angleedit.app.tools

import android.view.View
import android.widget.TextView
import com.rubyhuntersky.angleedit.app.RecentSource
import kotlinx.android.synthetic.main.fragment_recent_sources.view.*

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

class RecentSourcesViewHolder(val view: View) {

    fun bind(recentSources: List<RecentSource>, onClick: (RecentSource) -> Unit) {
        val sourceViews: List<TextView> = listOf(
                view.recentSource1 as TextView,
                view.recentSource2 as TextView,
                view.recentSource3 as TextView,
                view.recentSource4 as TextView,
                view.recentSource5 as TextView
        )
        sourceViews.forEachIndexed { i, view ->
            if (i < recentSources.size) {
                val recentSource = recentSources[i]
                view.text = recentSource.sourceUri.toString()
                view.setOnClickListener { onClick(recentSource) }
            } else {
                view.text = ""
                view.setOnClickListener(null)
            }
        }
    }
}