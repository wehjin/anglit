package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import rx.Completable
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.*

class MainActivity : AppCompatActivity(), XmlDocumentFragment.XmlInputStreamSource {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val DOCUMENT_ID_KEY = "document-id"
    }

    private val sampleInputStream: InputStream @Throws(IOException::class) get() = resources.assets.open("sample.xml")
    override val xmlInputStream: InputStream get() = openFileInput(document)
    var remoteInputStream: InputStream? = null
    var subscription: Subscription? = null
    var document: String? = null
    val documentInputStream: InputStream get() {
        if (remoteInputStream != null) {
            return remoteInputStream!!
        } else if (intent.data != null) {
            return FileInputStream(File(intent.data.path))
        } else {
            return sampleInputStream
        }
    }
    val Intent.sentUri: Uri? get() {
        if (action != Intent.ACTION_SEND) {
            return null
        }
        return Uri.parse(getStringExtra(Intent.EXTRA_TEXT)!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(DOCUMENT_ID_KEY, document)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val sentUri = intent.sentUri
            if (sentUri != null) {
                refreshWithUrl(sentUri.toString())
            } else {
                refresh()
            }
        } else {
            document = savedInstanceState.getString(DOCUMENT_ID_KEY)
            updateDisplay()
        }
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(MainActivity::class.java.simpleName, "New intent: " + intent)
        setIntent(intent)
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_recent) {
            refreshWithUrl("https://news.ycombinator.com/rss")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshWithUrl(url: String) {
        subscription?.unsubscribe()
        subscription = createRemoteInputStream(url)
                .subscribe({ refresh() }, { Log.e(TAG, "Recent", it) })
    }

    private fun refresh() {
        subscription?.unsubscribe()
        subscription = discardDocument()
                .andThen(getOrFetchDocument())
                .subscribe({ updateDisplay() }, { Log.e(TAG, "Refresh", it) })
    }

    private fun createRemoteInputStream(url: String): Completable {
        return Network.fetchStringToMain(url)
                .doOnNext {
                    Log.d(TAG, "Fetched:\n$it")
                    remoteInputStream = ByteArrayInputStream(it.toByteArray())
                }
                .toCompletable()
    }

    private fun updateDisplay() {
        supportFragmentManager.beginTransaction().replace(R.id.container, XmlDocumentFragment()).commit()
    }

    private fun getOrFetchDocument(): Completable {
        return Completable.defer {
            if (document != null) {
                Completable.complete()
            } else {
                fetchDocument().doOnNext { document = it }.toCompletable()
            }
        }
    }

    private fun fetchDocument(): Observable<String> {
        return Observable.create<String> { subscriber ->
            try {
                val documentId = IdGenerator.nextId()
                documentInputStream.use { externalStream ->
                    openFileOutput(documentId, Context.MODE_PRIVATE).use { internalStream ->
                        externalStream.copyTo(internalStream)
                    }
                }
                subscriber.onNext(documentId)
                subscriber.onCompleted()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    private fun discardDocument(): Completable {
        return Observable.just(document)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    if (document != null) {
                        deleteFile(it)
                    }
                }
                .onErrorResumeNext { Observable.empty() }
                .observeOn(AndroidSchedulers.mainThread())
                .toCompletable()
                .doOnCompleted { document = null }
    }

}
