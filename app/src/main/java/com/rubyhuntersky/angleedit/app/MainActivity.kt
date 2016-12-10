package com.rubyhuntersky.angleedit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.rubyhuntersky.angleedit.app.MainActivityMessage.*
import com.rubyhuntersky.angleedit.app.tools.alertDialog
import com.rubyhuntersky.angleedit.app.tools.bodyView
import com.rubyhuntersky.angleedit.app.tools.buttons
import com.rubyhuntersky.angleedit.app.tools.titleStringId
import kotlinx.android.synthetic.main.cell_source.view.*
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import java.sql.Timestamp

class MainActivity : AppCompatActivity() {

    val Intent.sentUri: Uri? get() = if (action == Intent.ACTION_SEND) Uri.parse(getStringExtra(Intent.EXTRA_TEXT)!!) else null
    val Intent.documentUri: Uri? get() = data ?: sentUri
    var subscription: Subscription? = null
    lateinit private var model: MainActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            model = MainActivityModel(null, null)
            update(ReadIntent)
        } else {
            model = savedInstanceState.getParcelable(MODEL_KEY)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(MODEL_KEY, model)
        super.onSaveInstanceState(outState)
    }

    fun update(message: MainActivityMessage) = when (message) {
        is ReadIntent -> setSource(intent.documentUri)
        is SetSource -> setSource(message.sourceUri)
        is SetDocument -> {
            deleteDocument(model.documentId)
            model.documentId = message.documentId
            displayModel()
        }
        is SetError -> {
            setSource(null)
            showError(message.place, message.throwable)
        }
    }

    private fun setSource(sourceUri: Uri?) {
        deleteDocument(model.documentId)
        model.sourceUri = sourceUri
        model.documentId = null
        displayModel()
    }

    private fun displayModel() {
        val nextFragment = if (model.documentId != null) {
            RecentSources.add(RecentSource(model.sourceUri!!, Timestamp(System.currentTimeMillis())))
            XmlDocumentFragment.create(model.documentId!!)
        } else if (model.sourceUri != null) {
            DocumentLoadingFragment.create(model.sourceUri!!)
        } else {
            RecentSourcesFragment()
        }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, nextFragment, ACTIVE_FRAGMENT)
                .commit()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "New intent: " + intent)
        setIntent(intent)
        update(ReadIntent)
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_open) {
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
                        onClick {
                            val sourceUri = Uri.parse(view.urlEditText.text.toString().trim())
                            update(SetSource(sourceUri))
                        }
                    }
                }
            }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteDocument(documentId: String?) {
        documentId ?: return
        Observable.just(documentId)
                .doOnNext { deleteFile(it) }
                .onErrorResumeNext { Observable.empty() }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun showError(place: String, t: Throwable) {
        Log.e(TAG, place, t)
        alertDialog(this@MainActivity, "$place \u2014 ${t.message}").show()
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val MODEL_KEY = "model-key"
        val ACTIVE_FRAGMENT = "active-fragment"
    }
}
