package com.rubyhuntersky.angleedit.app

/**
 * @author Jeffrey Yu
 * @since 12/11/16.
 */

sealed class XmlDocumentActivityMessage {
    class SetDocument(val documentId: String) : XmlDocumentActivityMessage()
    class SetError(val place: String, val throwable: Throwable) : XmlDocumentActivityMessage()
    class SetErrorMessage(val errorMessage: String) : XmlDocumentActivityMessage()
    object Close : XmlDocumentActivityMessage()
}