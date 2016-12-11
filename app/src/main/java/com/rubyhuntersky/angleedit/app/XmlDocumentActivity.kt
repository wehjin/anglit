package com.rubyhuntersky.angleedit.app

import DocumentCenter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.PersistableBundle
import com.rubyhuntersky.angleedit.app.XmlDocumentActivityMessage.*
import com.rubyhuntersky.angleedit.app.tools.BaseParcelable
import com.rubyhuntersky.angleedit.app.tools.read
import com.rubyhuntersky.angleedit.app.tools.write

class XmlDocumentActivity : BaseActivity() {

    val Intent.explicitSourceUri: Uri? get() = this.getParcelableExtra<Uri>(SOURCE_URI_KEY)
    val Intent.sentAsTextSourceUri: Uri? get() = if (action == Intent.ACTION_SEND) Uri.parse(getStringExtra(Intent.EXTRA_TEXT)!!) else null
    val Intent.sentAsDataSourceUri: Uri? get() = this.data
    lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_document)
        if (savedInstanceState == null) {
            val sourceUri = intent.explicitSourceUri ?: intent.sentAsDataSourceUri ?: intent.sentAsTextSourceUri
            model = Model(sourceUri!!, null)
            displayModel()
        } else {
            model = savedInstanceState.getParcelable("model-key")
        }
    }

    fun update(message: XmlDocumentActivityMessage) {
        when (message) {
            is SetDocument -> {
                model.documentId = message.documentId
                displayModel()
            }
            is SetError -> {
                showError(message.place, message.throwable)
                finish()
            }
            is Close -> {
                finish()
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_far, android.R.anim.slide_out_right)
    }

    override fun onDestroy() {
        if (isFinishing) {
            DocumentCenter.deleteDocument(model.documentId)
        }
        super.onDestroy()
    }

    private fun displayModel() {
        val nextFragment = if (model.documentId == null) {
            DocumentLoadingFragment.create(model.sourceUri)
        } else {
            XmlDocumentFragment.create(model.documentId!!)
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_xml_document, nextFragment, MainActivity.ACTIVE_FRAGMENT)
                .commit()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        outState.putParcelable("model-key", model)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    data class Model(val sourceUri: Uri, var documentId: String?) : BaseParcelable {
        override fun writeToParcel(outState: Parcel, flags: Int) {
            outState.write(sourceUri, documentId)
        }

        companion object {
            @Suppress("unused")
            @JvmField val CREATOR = BaseParcelable.generateCreator { Model(it.read(), it.read()) }
        }
    }

    companion object {
        const val SOURCE_URI_KEY: String = "source-uri-key"

        fun newIntent(context: Context, sourceUri: Uri): Intent {
            val intent = Intent(context, XmlDocumentActivity::class.java)
            intent.putExtra(XmlDocumentActivity.SOURCE_URI_KEY, sourceUri)
            return intent
        }
    }
}
