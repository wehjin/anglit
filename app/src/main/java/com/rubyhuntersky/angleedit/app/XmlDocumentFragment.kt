package com.rubyhuntersky.angleedit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import com.rubyhuntersky.angleedit.app.tools.firstTextString
import com.rubyhuntersky.angleedit.app.tools.isHttpUrl
import kotlinx.android.synthetic.main.fragment_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import rx.Observer
import rx.Subscription
import java.io.InputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


/**
 * @author wehjin
 * *
 * @since 8/2/14.
 */
class XmlDocumentFragment : BaseFragment() {
    data class Model(val document: Document, var isResumed: Boolean)

    val Node.elementNodes: List<Element> get() = (0..childNodes.length - 1)
            .map { childNodes.item(it) }
            .filter { it.nodeType == Node.ELEMENT_NODE }
            .map { it as Element }

    lateinit var model: Model
    var selections: Subscription? = null
    var selectedElement: Element? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is ActivityCreated -> initModel()
                is Resume -> resume()
                is Pause -> pause()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_main, container, false)!!

    private fun initModel() = try {
        val xmlInputStream = (activity as XmlInputStreamSource).xmlInputStream
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputStream)
        model = Model(document, isResumed = false)
    } catch (e: Throwable) {
        Toast.makeText(activity, "Format not supported: $e", Toast.LENGTH_LONG).show()
        val inputSource = InputSource(StringReader("<no-data/>"))
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
        model = Model(document, isResumed = false)
    }

    private fun resume() {
        model.isResumed = true
        displayModel()
    }

    private fun pause() {
        model.isResumed = false
        displayModel()
    }

    private fun displayModel() {
        if (model.isResumed) {
            treeView.adapter = model.toTreeViewAdapter
            selections = treeView.selections.subscribe(SelectionObserver())
            button_add_element.setOnClickListener {
                // TODO
            }
        } else {
            selections?.unsubscribe()
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

    private inner class SelectionObserver : Observer<Any> {
        override fun onError(e: Throwable) {
            Log.e(XmlDocumentFragment::class.java.simpleName, "Selections", e)
        }

        override fun onNext(tag: Any?) {
            val nextElement = tag as Element?
            if (nextElement != selectedElement && nextElement != null) {
                val firstTextString = nextElement.firstTextString
                if (firstTextString != null && firstTextString.isHttpUrl) {
                    val uri = Uri.parse(firstTextString)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            selectedElement = nextElement
        }

        override fun onCompleted() {
            // Do nothing
        }
    }

    interface XmlInputStreamSource {
        val xmlInputStream: InputStream
    }

    companion object {
        val TAG: String = XmlInputStreamSource::class.java.simpleName
    }
}
