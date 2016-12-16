package com.rubyhuntersky.angleedit.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.w3c.dom.Element

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

    fun enable(context: Context) {
        preferences = context.getSharedPreferences("model", 0)
        initModel()
    }

    fun containsAccent(element: Element): Boolean {
        return model.contains(element.asTagList)
    }

    fun removeAccent(element: Element?) {
        element ?: return
        model.remove(element.asTagList)
        saveModel()
    }

    fun addAccent(element: Element) {
        model.add(element.asTagList)
        saveModel()
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

