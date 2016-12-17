package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.*
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import com.rubyhuntersky.angleedit.app.TreeView.Tree
import com.rubyhuntersky.angleedit.app.XmlDocumentFragmentMessage.*
import com.rubyhuntersky.angleedit.app.data.AccentCenter
import com.rubyhuntersky.angleedit.app.data.DocumentCenter
import com.rubyhuntersky.angleedit.app.data.asTagList
import com.rubyhuntersky.angleedit.app.tools.*
import kotlinx.android.synthetic.main.fragment_xml_document.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import rx.subscriptions.CompositeSubscription


/**
 * @author wehjin
 * *
 * @since 8/2/14.
 */
class XmlDocumentFragment : BaseFragment() {

    val documentId: String get() = arguments.getString(DOCUMENT_ID_KEY)
    lateinit private var model: Model
    private val displaySubscriptions = CompositeSubscription()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("model-key", model)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is ActivityCreated -> {
                    model = savedInstanceState?.getParcelable<Model>("model-key") ?: Model(documentId)
                }
                is Resume -> {
                    try {
                        model.document
                        model.isResumed = true
                        display()
                    } catch (t: Throwable) {
                        (activity as? XmlDocumentActivity)?.update(XmlDocumentActivityMessage.SetError(TAG, t))
                    }
                }
                is Pause -> {
                    model.isResumed = false
                    display()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_xml_document, container, false)!!
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.fragment_xml_document, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_first -> update(ScrollToFirst)
        }
        return false
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
            is ScrollToFirst -> {
                val firstAccented = model.firstAccentedElement
                if (firstAccented != null) {
                    treeView.smoothScrollToTag(firstAccented)
                } else {
                    treeView.smoothScrollTo(0, 0)
                }
            }
        }
    }

    private fun display() {
        Log.v(TAG, "Display $model")
        if (model.isResumed) {
            treeView.adapter = model.asTreeViewAdapter
            if (model.scrollY == null) {
                treeView.post { treeView.scrollToTag(model.firstAccentedElement) }
            } else {
                treeView.scrollTo(0, model.scrollY!!)
            }
            displaySubscriptions.add(treeView.scrollTops.subscribe { update(TreeDidScroll(it)) })
            displaySubscriptions.add(treeView.clicks.subscribe { update(SelectElement(it as Element)) })
        } else {
            displaySubscriptions.clear()
        }
    }

    private val Model.asTreeViewAdapter: TreeView.Adapter get() {
        return object : TreeView.Adapter {
            override val tree: Tree get() = this@asTreeViewAdapter.tree

            override fun createView(): View {
                return ElementCellViewHolder(activity).itemView
            }

            override fun bindView(view: View, treeTag: Any) {
                val element = treeTag as Element
                ElementCellViewHolder(view).bind(
                        element = element,
                        isAccented = AccentCenter.containsAccent(element)
                )
                view.setOnLongClickListener {
                    if (AccentCenter.containsAccent(element)) {
                        AccentCenter.removeAccent(element)
                    } else {
                        AccentCenter.addAccent(element)
                    }
                    val changed = element.asTagList
                    treeView.notifyRowsChanged { treeTag -> (treeTag as Element).asTagList == changed }
                    true
                }
            }
        }
    }

    data class Model(val documentId: String,
                     var isResumed: Boolean = false,
                     var selectedElement: Element? = null,
                     var scrollY: Int? = null) : BaseParcelable {

        val document: Document by lazy { DocumentCenter.readDocument(documentId) }
        val firstAccentedElement: Element? get() = document.documentElement.flatten().find { AccentCenter.containsAccent(it) }
        val tree: Tree by lazy { document.documentElement.asTree() }

        private fun Element.asTree(): Tree = object : Tree {
            override val tag: Any get() = this@asTree
            override val subTrees: List<Tree> get() = elementNodes.map { it.asTree() }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.write(documentId, isResumed, scrollY)
            // TODO selectedElement
        }

        companion object {
            @Suppress("unused")
            val CREATOR = BaseParcelable.generateCreator { Model(it.read(), it.read(), it.read()) }
            // TODO selectedElement
        }
    }

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
