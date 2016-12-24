package com.rubyhuntersky.angleedit.app

import android.os.Parcel
import com.rubyhuntersky.angleedit.app.data.AccentCenter
import com.rubyhuntersky.angleedit.app.data.DocumentCenter
import com.rubyhuntersky.angleedit.app.tools.*
import org.w3c.dom.Document
import org.w3c.dom.Element

data class XmlDocumentFragmentModel(
        val documentId: String,
        var isResumed: Boolean = false,
        var selectedElement: Element? = null,
        var scrollY: Int? = null,
        var errorMessage: String? = null
) : BaseParcelable {

    val document: Document by lazy {
        DocumentCenter.readDocument(documentId)
    }

    val firstAccentedElement: Element?
        get() = document.documentElement.flatten().find { AccentCenter.containsAccent(it.asTagList) }

    val tree: TreeView.Tree by lazy {
        document.documentElement.asTree()
    }

    private fun Element.asTree(): TreeView.Tree = object : TreeView.Tree {
        override val tag: Any get() = this@asTree
        override val subTrees: List<TreeView.Tree> get() = elementNodes.map { it.asTree() }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.write(documentId, isResumed, scrollY, errorMessage)
        // TODO selectedElement
    }

    companion object {

        @Suppress("unused")
        val CREATOR = BaseParcelable.generateCreator {
            XmlDocumentFragmentModel(
                    documentId = it.read(),
                    isResumed = it.read(),
                    scrollY = it.read(),
                    errorMessage = it.read()
            )
        }
        // TODO selectedElement
    }
}