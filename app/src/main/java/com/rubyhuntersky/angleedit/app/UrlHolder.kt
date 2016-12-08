package com.rubyhuntersky.angleedit.app

import android.net.Uri
import rx.Observable
import rx.subjects.BehaviorSubject

/**
 * @author Jeffrey Yu
 * @since 12/5/16.
 */

object UrlHolder {
    data class UrlHolding(val uri: Uri?, val id: Int)

    private val subject = BehaviorSubject.create<UrlHolding>(UrlHolding(Uri.parse("assets:///sample.xml"), 0))

    var url: Uri?
        get() = subject.value.uri
        set(value) {
            val id = subject.value.id
            subject.onNext(UrlHolding(value, id + 1))
        }

    val urlHoldings: Observable<UrlHolding> get() = subject.asObservable()
}