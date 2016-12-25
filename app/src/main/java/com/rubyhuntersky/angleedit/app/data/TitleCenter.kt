package com.rubyhuntersky.angleedit.app.data

import rx.Observable
import rx.subjects.PublishSubject

/**
 * @author Jeffrey Yu
 * @since 12/24/16.
 */

object TitleCenter {

    private val tagLists = mutableMapOf(Pair("rss", listOf("title", "channel", "rss")))
    private val changeSubject = PublishSubject.create<Pair<String, List<String>?>>()

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
}