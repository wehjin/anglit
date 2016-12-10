package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rubyhuntersky.angleedit.app.MainActivityMessage.SetSource
import kotlinx.android.synthetic.main.fragment_recent_sources.view.*


/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

class RecentSourcesFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recent_sources, container, false)
        view.recentSourcesRecyclerView.layoutManager = LinearLayoutManager(context)
        view.recentSourcesRecyclerView.adapter = RecyclerViewAdapter(RecentSources.list())
        return view
    }

    inner class RecentSourceCellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(recentSource: RecentSource) {
            val textView = itemView as TextView
            textView.text = recentSource.sourceUri.toString()
            textView.setOnClickListener { (activity as MainActivity).update(SetSource(recentSource.sourceUri)) }
        }
    }

    inner class RecyclerViewAdapter(val recentSources: List<RecentSource>) : RecyclerView.Adapter<RecentSourceCellViewHolder>() {
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