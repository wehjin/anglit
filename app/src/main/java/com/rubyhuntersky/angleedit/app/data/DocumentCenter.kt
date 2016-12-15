package com.rubyhuntersky.angleedit.app.data

import android.content.Context
import org.w3c.dom.Document
import rx.Observable
import rx.schedulers.Schedulers
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Jeffrey Yu
 * @since 12/11/16.
 */

object DocumentCenter {

    lateinit private var context: Context
    private val preloads = mutableMapOf<String, Document>()

    fun enable(context: Context) {
        this.context = context
    }

    fun preloadDocument(documentId: String): Observable<String> = Observable.defer {
        val document = readDocument(documentId)
        synchronized(preloads) { preloads.put(documentId, document) }
        Observable.just(documentId)
    }

    fun readDocument(documentId: String): Document {
        val document = synchronized(preloads) { preloads.remove(documentId) }
        if (document != null) {
            return document
        } else {
            val inputStream = context.openFileInput(documentId)
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
        }
    }

    fun deleteDocument(documentId: String?) {
        documentId ?: return
        synchronized(preloads) { preloads.remove(documentId) }
        Observable.just(documentId)
                .doOnNext { context.deleteFile(it) }
                .onErrorResumeNext { Observable.empty() }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }
}