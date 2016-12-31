package com.rubyhuntersky.angleedit.app.xmldocumentfragment

import org.w3c.dom.Element

sealed class XmlDocumentFragmentMessage {
    object Start : XmlDocumentFragmentMessage()
    object Resume : XmlDocumentFragmentMessage()
    object Pause : XmlDocumentFragmentMessage()
    object Stop : XmlDocumentFragmentMessage()
    class SelectElement(val element: Element) : XmlDocumentFragmentMessage()
    class TreeDidScroll(val scrollTop: Int) : XmlDocumentFragmentMessage()
    object ScrollToFirst : XmlDocumentFragmentMessage()
}