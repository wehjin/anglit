package com.rubyhuntersky.angleedit.app

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rubyhuntersky.angleedit.app.MainActivityMessage.SetSource
import com.rubyhuntersky.angleedit.app.base.BaseFragment
import com.rubyhuntersky.angleedit.app.tools.AlertDialogButton
import com.rubyhuntersky.angleedit.app.tools.alertDialog
import com.rubyhuntersky.angleedit.app.tools.buttons
import com.rubyhuntersky.angleedit.app.tools.message
import kotlinx.android.synthetic.main.fragment_recent_sources.*
import kotlinx.android.synthetic.main.fragment_recent_sources.view.*


/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

class RecentSourcesFragment : BaseFragment() {

    init {
        lifecycleMessages.subscribe {
            when (it) {
                is FragmentLifecycleMessage.Start -> {
                    activity.setTitle(R.string.app_name)
                }
                is FragmentLifecycleMessage.Resume -> {
                    recentSourcesRecyclerView.adapter = RecyclerViewAdapter(RecentSources.list().toMutableList())
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recent_sources, container, false)
        view.recentSourcesRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    private fun removeRecentSource(recentSource: RecentSource) {
        val recyclerViewAdapter = recentSourcesRecyclerView.adapter as RecyclerViewAdapter
        val recentSources = recyclerViewAdapter.recentSources
        val index = recentSources.indexOf(recentSource)
        if (index != -1) {
            RecentSources.remove(recentSource)
            recentSources.removeAt(index)
            recyclerViewAdapter.notifyItemRemoved(index)
        }
    }

    inner class RecentSourceCellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(recentSource: RecentSource) {
            val textView = itemView as TextView
            textView.text = recentSource.sourceUri.toString()
            textView.setOnClickListener { (activity as MainActivity).update(SetSource(recentSource.sourceUri)) }
            textView.setOnLongClickListener {
                alertDialog(context) {
                    message = "${recentSource.sourceUri}"
                    buttons(AlertDialogButton.Negative("Cancel", DialogInterface::dismiss),
                            AlertDialogButton.Positive("Remove") { removeRecentSource(recentSource) })

                }.show()
                true
            }
        }
    }

    inner class RecyclerViewAdapter(val recentSources: MutableList<RecentSource>) : RecyclerView.Adapter<RecentSourceCellViewHolder>() {
        override fun getItemCount(): Int = recentSources.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSourceCellViewHolder {
            return RecentSourceCellViewHolder(activity.layoutInflater.inflate(R.layout.cell_recent_source, parent, false))
        }

        override fun onBindViewHolder(holder: RecentSourceCellViewHolder, position: Int) {
            holder.bind(recentSources[position])
        }
    }

    companion object {
        val TAG: String = RecentSourcesFragment::class.java.simpleName
    }
}