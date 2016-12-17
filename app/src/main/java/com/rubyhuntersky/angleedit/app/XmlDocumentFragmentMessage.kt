package com.rubyhuntersky.angleedit.app

import org.w3c.dom.Element

sealed class XmlDocumentFragmentMessage {
    class SelectElement(val element: Element) : XmlDocumentFragmentMessage()
    class TreeDidScroll(val scrollTop: Int) : XmlDocumentFragmentMessage()
    object ScrollToTop : XmlDocumentFragmentMessage()
}