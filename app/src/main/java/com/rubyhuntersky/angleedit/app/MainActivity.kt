package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.schedulers.Schedulers
import java.io.*

class MainActivity : AppCompatActivity(), XmlDocumentFragment.XmlInputStreamSource {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val DOCUMENT_ID_KEY = "document-id"
    }


    var remoteInputStream: InputStream? = null
    var subscription: Subscription? = null
    var documentSubscription: Subscription? = null
    var documentId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        documentId = savedInstanceState?.getString(DOCUMENT_ID_KEY)

        if (savedInstanceState == null) {
            loadFragment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(DOCUMENT_ID_KEY, documentId)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(MainActivity::class.java.simpleName, "New intent: " + intent)
        setIntent(intent)
        loadFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_recent) {
            loadRemoteDocument()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        subscription?.unsubscribe()
    }

    private fun deleteDocument() {
        if (documentId != null) {
            Observable.just(documentId)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { deleteFile(it) }
                    .onErrorResumeNext { Observable.empty() }
                    .subscribe()
            documentId = null
        }
    }

    private fun loadFragment() {
        documentSubscription?.unsubscribe()
        getOrFetchDocument().subscribe({
            supportFragmentManager.beginTransaction().replace(R.id.container, XmlDocumentFragment()).commit()
        }, { Log.e(TAG, "loadFragment", it) })
    }

    override val xmlInputStream: InputStream get() {
        return openFileInput(documentId)
    }

    private fun getOrFetchDocument(): Observable<String> {
        return Observable.defer {
            if (documentId != null) {
                Observable.just(documentId!!)
            } else {
                fetchDocument().doOnNext {
                    documentId = it
                }
            }
        }
    }

    private fun fetchDocument(): Observable<String> {
        return Observable.create<String> { subscriber ->
            try {
                val documentId = IdGenerator.nextId()
                externalDocumentInputStream.use { externalStream ->
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

    private fun loadRemoteDocument() {
        val url = "https://news.ycombinator.com/rss"
        subscription?.unsubscribe()
        subscription = Network.fetchStringToMain(url).subscribe(object : Observer<String> {
            override fun onError(e: Throwable?) {
                Log.e(TAG, "Recent", e)
            }

            override fun onNext(string: String) {
                Log.d(TAG, string)
                remoteInputStream = ByteArrayInputStream(string.toByteArray())
                deleteDocument()
                loadFragment()
            }

            override fun onCompleted() {
                // Do nothing
            }
        })
    }

    private val externalDocumentInputStream: InputStream get() {
        if (remoteInputStream != null) {
            return remoteInputStream!!
        } else if (intent.data != null) {
            return FileInputStream(File(intent.data.path))
        } else {
            return sampleInputStream
        }
    }

    private val sampleInputStream: InputStream
        @Throws(IOException::class)
        get() = resources.assets.open("sample.xml")

}
