package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.rubyhuntersky.angleedit.app.tools.*
import kotlinx.android.synthetic.main.cell_source.view.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), XmlDocumentFragment.XmlInputStreamSource {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val DOCUMENT_ID_KEY = "document-id"
    }

    override val xmlInputStream: InputStream get() = openFileInput(document)
    val Intent.sentUri: Uri? get() = if (action == Intent.ACTION_SEND) Uri.parse(getStringExtra(Intent.EXTRA_TEXT)!!) else null
    var subscription: Subscription? = null
    var document: String? by Delegates.observable(null as String?) { property, old, new ->
        if (old != null && old != new) {
            Observable.just(old)
                    .doOnNext { deleteFile(it) }
                    .onErrorResumeNext { Observable.empty() }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
        updateDisplay()
    }
    var urlSubscription: Subscription? = null

    private fun updateDisplay() {
        val xmlDocumentFragment = supportFragmentManager.findFragmentByTag(XmlDocumentFragment.TAG)
        if (document == null) {
            if (xmlDocumentFragment != null) {
                supportFragmentManager.beginTransaction()
                        .remove(xmlDocumentFragment)
                        .commit()
            }
        } else {
            if (xmlDocumentFragment == null) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.container, XmlDocumentFragment(), XmlDocumentFragment.TAG)
                        .commit()
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, XmlDocumentFragment(), XmlDocumentFragment.TAG)
                        .commit()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        outState.putString(DOCUMENT_ID_KEY, document)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        urlSubscription?.unsubscribe()
        urlSubscription = UrlHolder.urls
                .subscribe({
                    loadDocumentWithUri(it)
                }, {
                    showError("onStart", it)
                })
    }

    override fun onStop() {
        urlSubscription?.unsubscribe()
        super.onStop()
    }

    private fun loadDocumentWithUri(uri: Uri?) {
        subscription?.unsubscribe()
        subscription = (uri?.asDocument ?: Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    document = it
                }, {
                    document = null
                    showError("loadDocumentWithUri", it)
                })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "New intent: " + intent)
        setIntent(intent)
        parseIntent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            parseIntent()
        } else {
            document = savedInstanceState.getString(DOCUMENT_ID_KEY)
        }
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    private fun parseIntent() {
        if (intent.data != null) {
            UrlHolder.url = intent.data
        } else if (intent.sentUri != null) {
            UrlHolder.url = intent.sentUri
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_recent) {
            val dialogFragment = RecentSourcesDialogFragment()
            dialogFragment.show(supportFragmentManager, RecentSourcesDialogFragment.TAG)
            return true
        }
        if (item.itemId == R.id.action_change_source) {
            alertDialog(this) {
                val view = layoutInflater.inflate(R.layout.cell_source, null)
                titleStringId = R.string.change_source
                bodyView = view
                buttons {
                    negative {
                        label = "Cancel"
                    }
                    positive {
                        label = "Done"
                        onClick { UrlHolder.url = Uri.parse(view.urlEditText.text.toString().trim()) }
                    }
                }
            }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private val Uri.asDocument: Observable<String> get() = asInputStream.map { it.asDocument }

    private val Uri.asInputStream: Observable<InputStream> get() {
        return Observable.defer<InputStream> {
            val requiredScheme = scheme ?: ""
            when (requiredScheme.toLowerCase()) {
                "" -> Observable.error(RuntimeException("Invalid url: $this"))
                "http", "https" -> Network.fetchStringToMain(this).map { it.toByteArray() }.map(::ByteArrayInputStream)
                "assets" -> Observable.just(resources.assets.open(path.substring(1)))
                else -> Observable.just(contentResolver.openInputStream(this))
            }
        }
    }

    private val InputStream.asDocument: String get() {
        val documentId = IdGenerator.nextId()
        this.use { source ->
            openFileOutput(documentId, Context.MODE_PRIVATE).use { document -> source.copyTo(document) }
        }
        return documentId
    }

    private fun showError(place: String, t: Throwable) {
        Log.e(TAG, place, t)
        alertDialog(this@MainActivity, t.message ?: place).show()
    }
}
