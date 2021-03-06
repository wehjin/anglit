package com.rubyhuntersky.angleedit.app.mainactivity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.rubyhuntersky.angleedit.app.R
import com.rubyhuntersky.angleedit.app.base.BaseActivity
import com.rubyhuntersky.angleedit.app.mainactivity.MainActivityMessage.SetError
import com.rubyhuntersky.angleedit.app.mainactivity.MainActivityMessage.SetSource
import com.rubyhuntersky.angleedit.app.recentsourcesfragment.RecentSource
import com.rubyhuntersky.angleedit.app.recentsourcesfragment.RecentSources
import com.rubyhuntersky.angleedit.app.recentsourcesfragment.RecentSourcesFragment
import com.rubyhuntersky.angleedit.app.tools.*
import com.rubyhuntersky.angleedit.app.xmldocumentactivity.XmlDocumentActivity
import kotlinx.android.synthetic.main.cell_source.view.*
import java.sql.Timestamp

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            display()
        }
    }

    fun update(message: MainActivityMessage) = when (message) {
        is SetSource -> {
            RecentSources.add(RecentSource(message.sourceUri, Timestamp(System.currentTimeMillis())))
            startActivity(XmlDocumentActivity.newIntent(this as Context, message.sourceUri))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_fade_out)
        }
        is SetError -> {
            showError(message.place, message.throwable)
        }
    }

    private fun display() {
        val nextFragment = RecentSourcesFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, nextFragment, ACTIVE_FRAGMENT)
                .commit()
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
                buttons(AlertDialogButton.Negative("Cancel") {},
                        AlertDialogButton.Positive("Done") {
                            val sourceUri = Uri.parse(view.urlEditText.text.toString().trim())
                            update(SetSource(sourceUri))
                        })
            }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        val ACTIVE_FRAGMENT = "active-fragment"
    }
}
