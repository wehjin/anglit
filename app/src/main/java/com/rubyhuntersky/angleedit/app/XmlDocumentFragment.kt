package com.rubyhuntersky.angleedit.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import com.rubyhuntersky.angleedit.app.XmlDocumentFragment.Message.SelectElement
import com.rubyhuntersky.angleedit.app.XmlDocumentFragment.Message.TreeDidScroll
import com.rubyhuntersky.angleedit.app.tools.asHttpUri
import com.rubyhuntersky.angleedit.app.tools.elementNodes
import kotlinx.android.synthetic.main.fragment_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
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
            val documentTag: String?,
            var isResumed: Boolean,
            var selectedElement: Element? = null,
            var scrollY: Int = 0
    )

    sealed class Message {
        class SelectElement(val element: Element) : Message()
        class TreeDidScroll(val scrollTop: Int) : Message()
    }

    lateinit private var model: Model
    private val displaySubscriptions = CompositeSubscription()
    val documentTag: String? get() = model.documentTag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is ActivityCreated -> init()
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

    private fun init() = try {
        Log.d(TAG, "initModel")
        val streamSource = activity as XmlInputStreamSource
        val xmlInputStream = streamSource.xmlInputStream
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputStream)
        model = Model(document, documentTag = streamSource.xmlInputStreamId, isResumed = false)
    } catch (e: Throwable) {
        Toast.makeText(activity, "Format not supported: $e", Toast.LENGTH_LONG).show()
        val inputSource = InputSource(StringReader("<no-data/>"))
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
        model = Model(document, documentTag = "no-data", isResumed = false)
    }

    private fun update(message: Message) {
        when (message) {
            is SelectElement -> {
                val next = message.element
                val previous = model.selectedElement
                model.selectedElement = next
                if (next != previous) {
                    val httpUri = next.asHttpUri
                    if (httpUri != null) {
                        val intent = Intent(Intent.ACTION_VIEW, httpUri)
                        startActivity(intent)
                    }
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
            displaySubscriptions.add(treeView.scrollTop.subscribe { update(TreeDidScroll(it)) })
            displaySubscriptions.add(treeView.selections.subscribe { update(SelectElement(it as Element)) })
            button_add_element.setOnClickListener {
                // TODO
            }
        } else {
            displaySubscriptions.clear()
            button_add_element.setOnClickListener(null)
        }
    }

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

    interface XmlInputStreamSource {
        val xmlInputStream: InputStream
        val xmlInputStreamId: String?
    }

    companion object {
        val TAG: String = XmlDocumentFragment::class.java.simpleName
    }
}
