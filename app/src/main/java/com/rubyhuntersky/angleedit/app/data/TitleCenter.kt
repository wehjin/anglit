package com.rubyhuntersky.angleedit.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import rx.Observable
import rx.subjects.PublishSubject

/**
 * @author Jeffrey Yu
 * @since 12/24/16.
 */

object TitleCenter {

    private val tagLists = mutableMapOf<String, List<String>>()
    private val changeSubject = PublishSubject.create<Pair<String, List<String>?>>()
    private var preferences: SharedPreferences? = null
    private val TITLE_TAG_LISTS_KEY = "title-tag-lists"

    fun enablePersistence(context: Context) {
        preferences = context.getSharedPreferences("title-center", 0)
        initTagLists()
        changeSubject.subscribe {
            saveTagLists()
        }
    }

    fun getTitleTagListsOfRoot(rootTag: String): Observable<List<String>?> = changeSubject.asObservable()
            .filter { it.first == rootTag }
            .map { it.second }
            .startWith(tagLists[rootTag])

    fun isTitleTagList(tagList: List<String>?): Boolean {
        tagList ?: return false
        return tagLists.values.contains(tagList)
    }

    fun addTitleTagList(tagList: List<String>) {
        val rootTag = tagList.last()
        tagLists[rootTag] = tagList
        changeSubject.onNext(Pair(rootTag, tagList))
    }

    fun removeTitleTagList(tagList: List<String>) {
        val rootTag = tagList.last()
        val priorTagList = tagLists[rootTag]
        if (priorTagList == null || priorTagList != tagList) {
            return
        }

        tagLists.remove(rootTag)
        changeSubject.onNext(Pair(rootTag, null))
    }

    private fun initTagLists() {
        val json = preferences!!.getString(TITLE_TAG_LISTS_KEY, "{\"rss\": [\"title\", \"channel\", \"rss\"]}")
        tagLists.putAll(json.toTagLists())
    }

    private fun saveTagLists() {
        val json = tagLists.toJson()
        preferences?.edit()?.putString(TITLE_TAG_LISTS_KEY, json)?.apply()
    }

    private fun String.toTagLists(): Map<String, List<String>> {
        val tagLists = mutableMapOf<String, List<String>>()
        val jsonObject = JSONObject(this)
        jsonObject.keys().forEach { rootTag ->
            val jsonArray = jsonObject.getJSONArray(rootTag)!!
            tagLists[rootTag] = (0 until jsonArray.length()).map { i -> jsonArray.getString(i) }
        }
        return tagLists
    }

    private fun Map<String, List<String>>.toJson(): String {
        val jsonObject = JSONObject()
        this.forEach {
            val rootTag = it.key
            val tagList = it.value
            val jsonArray = JSONArray()
            tagList.forEachIndexed { i, tag -> jsonArray.put(i, tag) }
            jsonObject.put(rootTag, jsonArray)
        }
        return jsonObject.toString()
    }
}