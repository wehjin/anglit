package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import com.rubyhuntersky.angleedit.app.XmlDocumentFragmentMessage.SelectElement
import com.rubyhuntersky.angleedit.app.XmlDocumentFragmentMessage.TreeDidScroll
import com.rubyhuntersky.angleedit.app.data.DocumentCenter
import com.rubyhuntersky.angleedit.app.tools.attributeMap
import com.rubyhuntersky.angleedit.app.tools.elementNodes
import com.rubyhuntersky.angleedit.app.tools.firstTextString
import kotlinx.android.synthetic.main.fragment_xml_document.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import rx.subscriptions.CompositeSubscription
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


/**
 * @author wehjin
 * *
 * @since 8/2/14.
 */
class XmlDocumentFragment : BaseFragment() {

    val documentId: String get() = arguments.getString(DOCUMENT_ID_KEY)
    lateinit private var model: Model
    private val displaySubscriptions = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is ActivityCreated -> {
                    try {
                        val document = DocumentCenter.readDocument(documentId)
                        model = Model(document, isResumed = false)
                    } catch (e: Throwable) {
                        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader("<no-data/>")))
                        model = Model(document, isResumed = false)
                        (activity as? XmlDocumentActivity)?.update(XmlDocumentActivityMessage.SetError(TAG, e))
                    }
                }
                is Resume -> {
                    model.isResumed = true
                    display()
                }
                is Pause -> {
                    model.isResumed = false
                    display()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_xml_document, container, false)!!
    }

    private fun update(message: XmlDocumentFragmentMessage) {
        when (message) {
            is SelectElement -> {
                model.selectedElement = message.element
                val selectedElement = model.selectedElement ?: return
                val detailFragmentModel = ElementDetailDialogFragment.Model(
                        title = selectedElement.tagName,
                        description = selectedElement.firstTextString,
                        attributes = selectedElement.attributeMap
                )
                val detailFragment = ElementDetailDialogFragment.create(detailFragmentModel)
                detailFragment.show(fragmentManager, ElementDetailDialogFragment.TAG)
            }
            is TreeDidScroll -> model.scrollY = message.scrollTop
        }
    }

    private fun display() {
        Log.d(TAG, "Display $model")
        if (model.isResumed) {
            treeView.adapter = model.asTreeViewAdapter
            treeView.scrollTo(0, model.scrollY)
            displaySubscriptions.add(treeView.scrollTops.subscribe { update(TreeDidScroll(it)) })
            displaySubscriptions.add(treeView.clicks.subscribe { update(SelectElement(it as Element)) })
        } else {
            displaySubscriptions.clear()
        }
    }

    private val Model.asTreeViewAdapter: TreeView.Adapter get() {
        val documentElement = document.documentElement
        return object : TreeView.Adapter {
            override val tree: TreeView.Tree get() = documentElement.asTree

            override fun createView(): View {
                return ElementCellViewHolder(activity).itemView
            }

            override fun bindView(view: View, treeTag: Any) {
                ElementCellViewHolder(view).bind(treeTag as Element)
            }
        }
    }
    private val Element.asTree: TreeView.Tree get() = object : TreeView.Tree {
        override val tag: Any get() = this@asTree
        override val subTrees: List<TreeView.Tree> get() = elementNodes.map { it.asTree }
    }

    data class Model(
            val document: Document,
            var isResumed: Boolean,
            var selectedElement: Element? = null,
            var scrollY: Int = 0
    )

    companion object {
        val TAG: String = XmlDocumentFragment::class.java.simpleName
        val DOCUMENT_ID_KEY = "document-id-key"
        fun create(documentId: String): XmlDocumentFragment {
            val fragment = XmlDocumentFragment()
            fragment.arguments = documentId.toArguments
            return fragment
        }

        val String.toArguments: Bundle get() {
            val arguments = Bundle()
            arguments.putString(DOCUMENT_ID_KEY, this)
            return arguments
        }
    }
}
