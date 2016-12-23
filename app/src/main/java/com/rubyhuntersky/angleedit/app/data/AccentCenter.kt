package com.rubyhuntersky.angleedit.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.w3c.dom.Element
import rx.Observable
import rx.subjects.PublishSubject

/**
 * @author Jeffrey Yu
 * @since 12/15/16.
 */

val Element.asTagList: List<String>
    get() {
        val tagList = mutableListOf<String>()
        var todo: Element? = this
        while (todo != null) {
            tagList.add(todo.tagName)
            todo = todo.parentNode as? Element
        }
        return tagList.toList()
    }

object AccentCenter {

    lateinit private var preferences: SharedPreferences
    private val defaultAccents: Set<List<String>> get() = setOf(
            listOf("title", "channel", "rss"),
            listOf("title", "item", "channel", "rss")
    )
    private val model = mutableSetOf<List<String>>()
    private val changesSubject = PublishSubject.create<List<String>>()
    val changes: Observable<List<String>> get() = changesSubject.asObservable()

    fun enable(context: Context) {
        preferences = context.getSharedPreferences("model", 0)
        initModel()
    }

    fun containsAccent(tagList: List<String>): Boolean = model.contains(tagList)

    fun removeAccent(tagList: List<String>) {
        model.remove(tagList)
        saveModel()
        changesSubject.onNext(tagList)
    }

    fun addAccent(tagList: List<String>) {
        model.add(tagList)
        saveModel()
        changesSubject.onNext(tagList)
    }

    private fun initModel() {
        val savedAccents = preferences.getStringSet("stringSet", null)?.map {
            val jsonArray = JSONArray(it)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        }?.toSet()
        model.clear()
        model.addAll(savedAccents ?: defaultAccents)
    }

    private fun saveModel() {
        val stringSet = model.map { JSONArray(it.toTypedArray()).toString() }.toSet()
        preferences.edit().putStringSet("stringSet", stringSet).apply()
    }
}

