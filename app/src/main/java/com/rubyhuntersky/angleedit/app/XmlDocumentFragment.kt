package com.rubyhuntersky.angleedit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import rx.Observer
import rx.Subscription
import java.io.InputStream
import java.util.ArrayList
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author wehjin
 * *
 * @since 8/2/14.
 */
class XmlDocumentFragment : Fragment() {

    lateinit var treeView: TreeView
    var selections: Subscription? = null
    var selectedElement: Element? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)!!
        rootView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        treeView = rootView.findViewById(R.id.treeView) as TreeView

        val addElementButton = rootView.findViewById(R.id.button_add_element) as TextView
        addElementButton.setOnClickListener {
            /*
                ((LinearLayout) rootView.findViewById(R.id.elements_panel)).addView(
                        View.inflate(getActivity(), R.layout.cell_element, null));
                        */
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindDocument()
    }

    private fun bindDocument() {
        val document: Document
        try {
            val xmlInputStream = (activity as XmlInputStreamSource).xmlInputStream
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputStream)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        treeView.setModel(newTreeViewModel(document.documentElement))
    }

    override fun onResume() {
        super.onResume()
        treeView.selections.subscribe(object : Observer<Any> {
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
        })
    }

    override fun onPause() {
        selections?.unsubscribe()
        super.onPause()
    }

    private fun newTreeViewModel(element: Element): TreeView.TreeViewModel {

        val models = ArrayList<TreeView.TreeViewModel>()

        val childNodes = element.childNodes
        for (i in 0..childNodes.length - 1) {
            val item = childNodes.item(i)
            if (item.nodeType == Node.ELEMENT_NODE) {
                models.add(newTreeViewModel(item as Element))
            }
        }
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

    interface XmlInputStreamSource {
        val xmlInputStream: InputStream
    }
}
