package com.rubyhuntersky.angleedit.app.recentsourcesfragment

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import org.json.JSONObject
import java.sql.Timestamp

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

object RecentSources {

    lateinit var preferences: SharedPreferences
    private val defaultSources: Set<RecentSource> get() = listOf(
            "assets:///sample.xml",
            "https://news.ycombinator.com/rss",
            "http://feeds.arstechnica.com/arstechnica/index")
            .map { Uri.parse(it) }
            .map { RecentSource(it, Timestamp(System.currentTimeMillis())) }
            .toSet()
    val model = mutableSetOf<RecentSource>()

    fun enable(context: Context) {
        preferences = context.getSharedPreferences("recent-sources", 0)
        initModel()
    }

    fun list(): List<RecentSource> {
        val visitedUris = mutableSetOf<Uri>()
        return model.sortedByDescending { it.accessTime }.filter {
            if (visitedUris.contains(it.sourceUri)) {
                false
            } else {
                visitedUris.add(it.sourceUri)
                true
            }
        }
    }

    fun add(recentSource: RecentSource) {
        model.removeAll { it.sourceUri == recentSource.sourceUri }
        model.add(recentSource)
        saveModel()
    }

    fun remove(recentSource: RecentSource) {
        if (model.removeAll { it.sourceUri == recentSource.sourceUri }) {
            saveModel()
        }
    }

    private fun initModel() {
        val savedStringSet = preferences.getStringSet("stringSet", emptySet())
        model.clear()
        model.addAll(if (savedStringSet.isEmpty()) {
            defaultSources
        } else {
            savedStringSet.map(::JSONObject).map(::RecentSource).toSet()
        })
    }

    private fun saveModel() {
        val stringSet = model.map { it.toJSONObject().toString() }.toSet()
        preferences.edit().putStringSet("stringSet", stringSet).apply()
    }
}