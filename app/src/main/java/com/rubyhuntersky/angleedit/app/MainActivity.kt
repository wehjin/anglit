package com.rubyhuntersky.angleedit.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import rx.Observer
import rx.Subscription
import java.io.*

class MainActivity : AppCompatActivity(), XmlDocumentFragment.XmlInputStreamSource {

    var remoteInputStream: InputStream? = null
    var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            loadFragment()
        }
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
                loadFragment()
            }

            override fun onCompleted() {
                // Do nothing
            }
        })
    }

    private fun loadFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.container, XmlDocumentFragment()).commit()
    }

    @Throws(IOException::class)
    override fun getXmlInputStream(): InputStream {
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

    companion object {

        val TAG: String = MainActivity::class.java.simpleName
    }

}
