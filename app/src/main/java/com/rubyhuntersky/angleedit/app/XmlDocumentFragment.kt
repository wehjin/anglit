package com.rubyhuntersky.angleedit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import rx.Observer
import rx.Subscription
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.properties.Delegates


/**
 * @author wehjin
 * *
 * @since 8/2/14.
 */
class XmlDocumentFragment : BaseFragment() {
    data class Model(val document: Document)

    val Node.elementNodes: List<Node> get() = (0..childNodes.length - 1)
            .map { childNodes.item(it) }
            .filter { it.nodeType == Node.ELEMENT_NODE }

    lateinit var model: Model
    var selections: Subscription? = null
    var selectedElement: Element? = null
    var isDisplayEnabled: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue && !oldValue) {
            displayIfEnabled()
        } else if (oldValue && !newValue) {
            endDisplay()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleMessages.subscribe { message ->
            when (message) {
                is ActivityCreated -> {
                    initModel()
                    bind()
                }
                is Resume -> isDisplayEnabled = true
                is Pause -> isDisplayEnabled = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_main, container, false)!!

    private fun bind() = treeView.setModel(newTreeViewModel(model.document.documentElement))

    private fun displayIfEnabled() {
        activity.title = model.document.documentElement.tagName
        selections = treeView.selections.subscribe(SelectionObserver())
        button_add_element.setOnClickListener {
            /*
                ((LinearLayout) rootView.findViewById(R.id.elements_panel)).addView(
                        View.inflate(getActivity(), R.layout.cell_element, null));
                        */
        }
    }

    private fun endDisplay() {
        selections?.unsubscribe()
        button_add_element.setOnClickListener(null)
    }

    private fun initModel() {
        val document: Document
        try {
            val xmlInputStream = (activity as XmlInputStreamSource).xmlInputStream
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputStream)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        model = Model(document)
    }

    private fun newTreeViewModel(element: Element): TreeView.TreeViewModel {
        val models = element.elementNodes.map { newTreeViewModel(it as Element) }
        return object : TreeView.TreeViewModel {
            override fun newViewInstance(): View {
                val viewHolder = ElementCellViewHolder(activity)
                viewHolder.bind(element)
                return viewHolder.itemView
            }

            override fun getTag(): Any {
                return element
            }

            override fun getChildModels(): List<TreeView.TreeViewModel> {
                return models
            }
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
}
