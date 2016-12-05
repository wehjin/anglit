package com.rubyhuntersky.angleedit.app

import rx.Observable
import rx.subjects.BehaviorSubject

/**
 * @author Jeffrey Yu
 * @since 12/5/16.
 */

object UrlHolder {
    private val urlSubject = BehaviorSubject.create<String?>()

    var url: String?
        get() = urlSubject.value
        set(value) {
            urlSubject.onNext(value)
        }

    val urlAndChanges: Observable<String?> get() = urlSubject.asObservable()
}