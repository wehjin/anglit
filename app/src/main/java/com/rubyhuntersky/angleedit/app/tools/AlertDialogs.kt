package com.rubyhuntersky.angleedit.app.tools

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import com.rubyhuntersky.angleedit.app.tools.AlertDialogButton.Positive

/**
 * @author Jeffrey Yu
 * @since 12/5/16.
 */

fun alertDialog(context: Context, init: AlertDialog.() -> Unit): AlertDialog {
    val alertDialog = AlertDialog.Builder(context).create()
    return alertDialog.apply { init() }
}

fun messageDialog(context: Context, text: String): AlertDialog = alertDialog(context) {
    message = text
    buttons(Positive("Close") {})

}

fun errorDialog(activity: Activity, place: String, throwable: Throwable): AlertDialog = alertDialog(activity) {
    message = "$place \u2014 ${throwable.javaClass.simpleName}\n${throwable.message}"
    buttons(Positive("Close") {})
    dismiss { activity.finish() }
}

fun AlertDialog.dismiss(onDismiss: () -> Unit) = setOnDismissListener { onDismiss() }

fun AlertDialog.buttons(vararg buttons: AlertDialogButton) {
    buttons.forEach {
        setButton(it.which, it.label, { dialog, which -> it.onClick(dialog) })
    }
}

var AlertDialog.message: CharSequence
    get() = throw UnsupportedOperationException("get AlertDialog.label")
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

sealed class AlertDialogButton {
    abstract val label: String
    abstract val onClick: (DialogInterface) -> Unit
    abstract val which: Int

    class Positive(override val label: String, override val onClick: (DialogInterface) -> Unit) : AlertDialogButton() {
        override val which = AlertDialog.BUTTON_POSITIVE
    }

    class Neutral(override val label: String, override val onClick: (DialogInterface) -> Unit) : AlertDialogButton() {
        override val which = AlertDialog.BUTTON_NEUTRAL
    }

    class Negative(override val label: String, override val onClick: (DialogInterface) -> Unit) : AlertDialogButton() {
        override val which = AlertDialog.BUTTON_NEGATIVE
    }
}
