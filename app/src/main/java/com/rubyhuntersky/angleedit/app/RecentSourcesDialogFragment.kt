package com.rubyhuntersky.angleedit.app

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_recent_sources.view.*

/**
 * @author Jeffrey Yu
 * @since 12/7/16.
 */

class RecentSourcesDialogFragment : BottomSheetDialogFragment() {

    val cellModels: List<String> = listOf(
            "https://news.ycombinator.com/rss",
            "http://feeds.arstechnica.com/arstechnica/index"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflate = inflater.inflate(R.layout.fragment_recent_sources, container, false)
        val cellViews: List<TextView> = listOf(
                inflate.recentSource1 as TextView,
                inflate.recentSource2 as TextView,
                inflate.recentSource3 as TextView
        )
        cellViews.forEachIndexed { i, view ->
            if (i < cellModels.size) {
                view.text = cellModels[i]
                view.setOnClickListener {
                    dismiss()
                    changeSource(cellModels[i])
                }
            } else {
                view.text = ""
                view.setOnClickListener(null)
            }
        }
        return inflate
    }

    private fun changeSource(uriString: String) {
        UrlHolder.url = Uri.parse(uriString)
    }

    companion object {
        val TAG: String = RecentSourcesDialogFragment::class.java.simpleName
    }
}