package com.rubyhuntersky.angleedit.app

import com.rubyhuntersky.angleedit.app.data.DocumentCenter
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.Pause
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.Resume
import com.rubyhuntersky.angleedit.app.XmlDocumentActivityMessage.SetDocument
import com.rubyhuntersky.angleedit.app.XmlDocumentActivityMessage.SetError
import com.rubyhuntersky.angleedit.app.tools.IdGenerator
import com.rubyhuntersky.angleedit.app.tools.Network
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

class DocumentLoadingFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_document_loading, container, false)
    }

    val sourceUri: Uri get() = arguments.getParcelable(SOURCE_URI_KEY)
    val xmlActivity: XmlDocumentActivity get() = activity as XmlDocumentActivity
    var subscription: Subscription? = null

    init {
        lifecycleMessages.subscribe {
            when (it) {
                is Resume -> {
                    subscription = sourceUri.asDocument
                            .zipWith(Observable.timer(1200, TimeUnit.MILLISECONDS), { documentId, time ->
                                documentId
                            })
                            .flatMap { DocumentCenter.preloadDocument(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                xmlActivity.update(SetDocument(it))
                            }, {
                                xmlActivity.update(SetError("DocumentLoadingFragment", it))
                            })
                }
                is Pause -> {
                    subscription?.unsubscribe()
                }
            }
        }
    }

    private val Uri.asDocument: Observable<String> get() = this.asInputStream.map { it.asDocument }

    private val Uri.asInputStream: Observable<InputStream> get() {
        return Observable.defer<InputStream> {
            val requiredScheme = scheme ?: ""
            when (requiredScheme.toLowerCase()) {
                "" -> Observable.error(RuntimeException("Invalid url: $this"))
                "http", "https" -> Network.fetchStringToMain(this).map { it.toByteArray() }.map(::ByteArrayInputStream)
                "assets" -> Observable.just(resources.assets.open(path.substring(1)))
                else -> Observable.just(activity.contentResolver.openInputStream(this))
            }
        }
    }

    private val InputStream.asDocument: String get() {
        val documentId = IdGenerator.nextId()
        this.use { source ->
            activity.openFileOutput(documentId, Context.MODE_PRIVATE).use { document -> source.copyTo(document) }
        }
        return documentId
    }


    companion object {
        val SOURCE_URI_KEY = "source-uri-key"

        fun create(sourceUri: Uri): DocumentLoadingFragment {
            val fragment = DocumentLoadingFragment()
            fragment.arguments = sourceUri.toArguments
            return fragment
        }

        val Uri.toArguments: Bundle get() {
            val arguments = Bundle()
            arguments.putParcelable(SOURCE_URI_KEY, this)
            return arguments
        }
    }
}