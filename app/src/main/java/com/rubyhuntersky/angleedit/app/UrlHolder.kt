package com.rubyhuntersky.angleedit.app

import android.net.Uri
import rx.Observable
import rx.subjects.BehaviorSubject

/**
 * @author Jeffrey Yu
 * @since 12/5/16.
 */

object UrlHolder {
    private val urlSubject = BehaviorSubject.create<Uri?>(Uri.parse("assets:///sample.xml"))

    var url: Uri?
        get() = urlSubject.value
        set(value) {
            urlSubject.onNext(value)
        }

    val urls: Observable<Uri?> get() = urlSubject.asObservable()
}