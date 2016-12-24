package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import com.rubyhuntersky.angleedit.app.XmlDocumentActivityMessage.*
import com.rubyhuntersky.angleedit.app.data.DocumentCenter
import com.rubyhuntersky.angleedit.app.tools.BaseParcelable
import com.rubyhuntersky.angleedit.app.tools.read
import com.rubyhuntersky.angleedit.app.tools.write

class XmlDocumentActivity : BaseActivity() {

    val Intent.explicitSourceUri: Uri? get() = this.getParcelableExtra<Uri>(SOURCE_URI_KEY)
    val Intent.sentAsTextSourceUri: Uri? get() = if (action == Intent.ACTION_SEND) Uri.parse(getStringExtra(Intent.EXTRA_TEXT)!!) else null
    val Intent.sentAsDataSourceUri: Uri? get() = this.data
    private val MODEL_KEY = "model-key"
    lateinit var model: Model

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(MODEL_KEY, model)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_document)
        if (savedInstanceState == null) {
            val sourceUri = intent.explicitSourceUri ?: intent.sentAsDataSourceUri ?: intent.sentAsTextSourceUri
            model = Model(sourceUri!!, null)
            displayModel()
        } else {
            model = savedInstanceState.getParcelable(MODEL_KEY)
        }
    }

    fun update(message: XmlDocumentActivityMessage) {
        when (message) {
            is SetDocument -> {
                model.documentId = message.documentId
                displayModel()
            }
            is SetError -> {
                removeActiveFragment()
                showError(message.place, message.throwable)
            }
            is SetErrorMessage -> {
                removeActiveFragment()
                showErrorMessage(message.errorMessage)
            }
            is Close -> {
                finish()
            }
        }
    }

    private fun removeActiveFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(MainActivity.ACTIVE_FRAGMENT)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_fade_in, R.anim.slide_out_right)
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
        Log.d(TAG, "displayModel")
    }

    data class Model(val sourceUri: Uri, var documentId: String?)
        : BaseParcelable {

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.write(sourceUri, documentId)
        }

        companion object {
            @Suppress("unused")
            @JvmField val CREATOR = BaseParcelable.generateCreator { Model(it.read(), it.read()) }
        }
    }

    companion object {
        const val SOURCE_URI_KEY: String = "source-uri-key"
        val TAG: String = XmlDocumentActivity::class.java.simpleName

        fun newIntent(context: Context, sourceUri: Uri): Intent {
            val intent = Intent(context, XmlDocumentActivity::class.java)
            intent.putExtra(XmlDocumentActivity.SOURCE_URI_KEY, sourceUri)
            return intent
        }
    }
}
