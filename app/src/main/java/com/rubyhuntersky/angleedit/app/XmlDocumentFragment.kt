package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import com.rubyhuntersky.angleedit.app.MainActivityMessage.SetSource
import com.rubyhuntersky.angleedit.app.XmlDocumentFragment.Message.SelectElement
import com.rubyhuntersky.angleedit.app.XmlDocumentFragment.Message.TreeDidScroll
import com.rubyhuntersky.angleedit.app.tools.elementNodes
import com.rubyhuntersky.angleedit.app.tools.firstTextString
import kotlinx.android.synthetic.main.fragment_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.io.InputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


/**
 * @author wehjin
 * *
 * @since 8/2/14.
 */
class XmlDocumentFragment : BaseFragment() {

    data class Model(
            val document: Document,
            var isResumed: Boolean,
            var selectedElement: Element? = null,
            var scrollY: Int = 0
    )

    sealed class Message {
        class SelectElement(val element: Element) : Message()
        class TreeDidScroll(val scrollTop: Int) : Message()
    }

    val String.asXmlInputStream: InputStream get() = activity.openFileInput(this)
    val documentId: String get() = arguments.getString(DOCUMENT_ID_KEY)
    lateinit private var model: Model
    private val displaySubscriptions = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is ActivityCreated -> initModel()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_main, container, false)!!
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.fragment_xml_document, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_close) {
            (activity as MainActivity).update(SetSource(null))
            return true
        } else {
            return false
        }
    }

    private fun initModel() = try {
        Log.d(TAG, "initModel")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(documentId.asXmlInputStream)
        model = Model(document, isResumed = false)
    } catch (e: Throwable) {
        Toast.makeText(activity, "Format not supported: $e", Toast.LENGTH_LONG).show()
        val inputSource = InputSource(StringReader("<no-data/>"))
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
        model = Model(document, isResumed = false)
    }

    private fun update(message: Message) {
        when (message) {
            is SelectElement -> {
                model.selectedElement = message.element
                val selectedElement = model.selectedElement
                if (selectedElement != null) {
                    val fragment = ElementDetailDialogFragment.create(selectedElement.asFragmentModel)
                    fragment.show(fragmentManager, ElementDetailDialogFragment.TAG)
                }
            }
            is TreeDidScroll -> model.scrollY = message.scrollTop
        }
    }

    private fun display() {
        Log.d(TAG, "Display $model")
        if (model.isResumed) {
            treeView.adapter = model.toTreeViewAdapter
            treeView.scrollTo(0, model.scrollY)
            treeView.scrollTops.subscribe { update(TreeDidScroll(it)) }.whileDisplayed()
            treeView.clicks.subscribe { update(SelectElement(it as Element)) }.whileDisplayed()
            button_add_element.setOnClickListener {
                // TODO
            }
        } else {
            displaySubscriptions.clear()
            button_add_element.setOnClickListener(null)
        }
    }

    private val Element.asFragmentModel: ElementDetailDialogFragment.Model get() = ElementDetailDialogFragment.Model(tagName, firstTextString)
    private fun Subscription.whileDisplayed() = displaySubscriptions.add(this)
    private val Model.toTreeViewAdapter: TreeView.Adapter get() = document.documentElement.toTreeViewAdapter
    private val Element.toTreeViewAdapter: TreeView.Adapter get() = object : TreeView.Adapter {
        override val tag: Any get() = this@toTreeViewAdapter
        override val subAdapters: List<TreeView.Adapter> get() = elementNodes.map { it.toTreeViewAdapter }
        override fun newViewInstance(): View {
            val viewHolder = ElementCellViewHolder(activity)
            viewHolder.bind(this@toTreeViewAdapter)
            return viewHolder.itemView
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
