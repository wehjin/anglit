package com.rubyhuntersky.angleedit.app

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

object Network {

    fun fetchStringToMain(url: String): Observable<String> {
        return Observable.create(Observable.OnSubscribe<String> { subscriber ->
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response: Response
            try {
                response = client.newCall(request).execute()
                val string = response.body().string()
                subscriber.onNext(string)
                subscriber.onCompleted()
            } catch (e: IOException) {
                subscriber.onError(e)
            }
        }).subscribeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread())
    }
}