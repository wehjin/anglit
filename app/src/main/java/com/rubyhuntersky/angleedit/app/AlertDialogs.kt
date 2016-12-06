package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View

/**
 * @author Jeffrey Yu
 * @since 12/5/16.
 */

fun alertDialog(context: Context, init: AlertDialog.() -> Unit): AlertDialog {
    return AlertDialog.Builder(context).create().apply { init() }
}

fun alertDialog(context: Context, text: String): AlertDialog = alertDialog(context) {
    message = text
    buttons {
        positive("Close")
    }
}

class AlertDialogButton(val buttonId: Int) {
    lateinit var label: CharSequence
    internal var onClickListener: (DialogInterface) -> Unit = {}

    fun onClick(listener: (DialogInterface) -> Unit) {
        this.onClickListener = listener
    }
}

class AlertDialogButtons {
    var positiveButton: AlertDialogButton? = null
    var negativeButton: AlertDialogButton? = null
    var neutralButton: AlertDialogButton? = null
    val buttons: List<AlertDialogButton> get() = listOf(positiveButton, negativeButton, neutralButton).filterNotNull()

    fun positive(initButton: AlertDialogButton.() -> Unit) {
        positiveButton = AlertDialogButton(AlertDialog.BUTTON_POSITIVE).apply { initButton() }
    }

    fun positive(label: String) = positive { this.label = label }

    fun negative(initButton: AlertDialogButton.() -> Unit) {
        negativeButton = AlertDialogButton(AlertDialog.BUTTON_NEGATIVE).apply { initButton() }
    }

    fun negative(label: String) = negative { this.label = label }

    fun neutral(initButton: AlertDialogButton.() -> Unit) {
        neutralButton = AlertDialogButton(AlertDialog.BUTTON_NEUTRAL).apply { initButton() }
    }

    fun neutral(label: String) = neutral { this.label = label }
}

fun AlertDialog.buttons(initButtons: AlertDialogButtons.() -> Unit) {
    AlertDialogButtons().apply { initButtons() }.buttons.forEach {
        setButton(it.buttonId, it.label, { dialog, which ->
            it.onClickListener(dialog)
        })
    }
}

var AlertDialog.message: CharSequence
    get() = throw UnsupportedOperationException("get AlertDialog.message")
    set(value) = setMessage(value)

var AlertDialog.messageStringId: Int
    get() = throw UnsupportedOperationException("get AlertDialog.messageId")
    set(value) {
        message = context.getString(value)
    }

var AlertDialog.title: CharSequence
    get() = throw UnsupportedOperationException("get AlertDialog.title")
    set(value) = setTitle(value)

var AlertDialog.titleStringId: Int
    get() = throw UnsupportedOperationException("get AlertDialog.titleStringId")
    set(value) {
        title = context.getString(value)
    }

var AlertDialog.bodyView: View
    get() = throw UnsupportedOperationException("get AlertDialog.bodyView")
    set(value) = setView(value)

var AlertDialog.bodyLayoutId: Int
    get() = throw UnsupportedOperationException("get AlertDialog.videId")
    set(value) {
        bodyView = layoutInflater.inflate(value, null)
    }
