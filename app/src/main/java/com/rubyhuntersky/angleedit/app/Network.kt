package com.rubyhuntersky.angleedit.app

import okhttp3.OkHttpClient
import okhttp3.Request
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

object Network {

    fun fetchStringToMain(url: String): Observable<String> {
        return Observable.create(Observable.OnSubscribe<String> { subscriber ->
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                subscriber.onNext(response.body().string())
                subscriber.onCompleted()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}