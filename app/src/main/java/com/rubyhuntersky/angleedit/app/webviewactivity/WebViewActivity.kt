package com.rubyhuntersky.angleedit.app.webviewactivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebViewClient
import com.rubyhuntersky.angleedit.app.R
import com.rubyhuntersky.angleedit.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : BaseActivity() {
    companion object {
        const val INTENT_URL_KEY = "url-key"
    }

    val url: String get() = intent.getStringExtra(INTENT_URL_KEY)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        webView.settings.javaScriptEnabled = true
        webView.setWebViewClient(object : WebViewClient() {
        })
        if (savedInstanceState == null) {
            webView.loadUrl(url)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_web_view, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_clear_cookies -> {
                CookieManager.getInstance().removeAllCookies {
                    webView.clearCache(true)
                    webView.clearHistory()
                    webView.loadUrl(url)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }
}
