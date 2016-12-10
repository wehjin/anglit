package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rubyhuntersky.angleedit.app.MainActivityMessage.SetSource
import com.rubyhuntersky.angleedit.app.tools.RecentSourcesViewHolder

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

class RecentSourcesFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_recent_sources, container, false)
        val recentSourcesViewHolder = RecentSourcesViewHolder(view)
        recentSourcesViewHolder.bind(RecentSources.list()) {
            Log.d(TAG, "Click: $it")
            (activity as MainActivity).update(SetSource(it.sourceUri))
        }
        return view
    }

    companion object {
        val TAG: String = RecentSourcesFragment::class.java.simpleName
    }
}