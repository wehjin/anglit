package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.util.Log
import android.view.*
import com.rubyhuntersky.angleedit.app.TreeView.Tree
import com.rubyhuntersky.angleedit.app.XmlDocumentFragmentMessage.*
import com.rubyhuntersky.angleedit.app.data.AccentCenter
import com.rubyhuntersky.angleedit.app.data.TitleCenter
import com.rubyhuntersky.angleedit.app.tools.*
import kotlinx.android.synthetic.main.fragment_xml_document.*
import org.w3c.dom.Element
import rx.Observable
import rx.subscriptions.CompositeSubscription


/**
 * @author wehjin
 * @since 8/2/14.
 */
class XmlDocumentFragment : BaseFragment() {

    val documentId: String get() = arguments.getString(DOCUMENT_ID_KEY)
    lateinit private var model: XmlDocumentFragmentModel
    private val displaySubscriptions = CompositeSubscription()
    private val untilStoppedSubscriptions = CompositeSubscription()
    private fun <T> Observable<T>.subscribeUntilStopped(onNext: (T) -> Unit) = untilStoppedSubscriptions.add(this.subscribe(onNext))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is FragmentLifecycleMessage.ActivityCreated -> {
                    model = message.savedState?.getParcelable<XmlDocumentFragmentModel>("model-key") ?: XmlDocumentFragmentModel(documentId)
                }
                is FragmentLifecycleMessage.SaveInstanceState -> {
                    message.outState.putParcelable("model-key", model)
                }
                is FragmentLifecycleMessage.Start -> update(Start)
                is FragmentLifecycleMessage.Resume -> update(Resume)
                is FragmentLifecycleMessage.Pause -> update(Pause)
                is FragmentLifecycleMessage.Stop -> update(Stop)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_xml_document, container, false)!!
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.fragment_xml_document, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_first -> update(ScrollToFirst)
        }
        return false
    }

    private fun update(message: XmlDocumentFragmentMessage) {
        when (message) {
            is Start -> {
                try {
                    val document = model.document
                    TitleCenter.getTitleTagListsOfRoot(document.documentElement.tagName).subscribeUntilStopped {
                        val titleElement = document.documentElement.findDescendantWithTagList(it)
                        val title = titleElement?.toTitleText ?: getString(R.string.app_name)
                        activity.title = title
                    }
                    AccentCenter.changes.subscribeUntilStopped { changed ->
                        treeView.notifyRowsChanged { treeTag -> (treeTag as Element).asTagList == changed }
                    }
                } catch (t: Throwable) {
                    model.errorMessage = t.toErrorMessage(TAG)
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
            is Stop -> {
                untilStoppedSubscriptions.clear()
            }
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
            displayResumedModel()
        } else {
            displayPausedModel()
        }
    }

    private fun displayResumedModel() {
        val errorMessage = model.errorMessage
        if (errorMessage != null) {
            (activity as? XmlDocumentActivity)?.update(XmlDocumentActivityMessage.SetErrorMessage(errorMessage))
            return
        }

        treeView.adapter = model.asTreeViewAdapter
        if (model.scrollY == null) {
            treeView.post { treeView.scrollToTag(model.firstAccentedElement) }
        } else {
            treeView.scrollTo(0, model.scrollY!!)
        }
        displaySubscriptions.add(treeView.scrollTops.subscribe { update(TreeDidScroll(it)) })
        displaySubscriptions.add(treeView.clicks.subscribe { update(SelectElement(it as Element)) })
    }

    private fun displayPausedModel() {
        displaySubscriptions.clear()
    }

    private val XmlDocumentFragmentModel.asTreeViewAdapter: TreeView.Adapter get() {
        return object : TreeView.Adapter {
            override val tree: Tree get() = this@asTreeViewAdapter.tree

            override fun createView(): View {
                return ElementCellViewHolder(activity).itemView
            }

            override fun bindView(view: View, treeTag: Any) {
                val element = treeTag as Element
                ElementCellViewHolder(view).bind(element)
                view.setOnLongClickListener {
                    val dialogFragment = TagListDetailsDialogFragment(element.asTagList)
                    dialogFragment.show(fragmentManager, "taglist-details")
                    true
                }
            }
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
