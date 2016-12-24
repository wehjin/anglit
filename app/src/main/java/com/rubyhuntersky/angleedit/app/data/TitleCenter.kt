package com.rubyhuntersky.angleedit.app.data

import rx.Observable

/**
 * @author Jeffrey Yu
 * @since 12/24/16.
 */

object TitleCenter {

    private val titleTagLists = mapOf(Pair("rss", listOf("title", "channel", "rss")))

    fun getTitleTagListsForRootTag(rootTag: String): Observable<List<String>?> {
        return Observable.just(titleTagLists[rootTag])
    }

    fun isTitleTagList(tagList: List<String>?): Boolean {
        tagList ?: return false
        return titleTagLists.values.contains(tagList)
    }
}