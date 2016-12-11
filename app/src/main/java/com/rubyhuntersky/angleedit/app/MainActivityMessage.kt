package com.rubyhuntersky.angleedit.app

import android.net.Uri

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

sealed class MainActivityMessage {
    class SetSource(val sourceUri: Uri) : MainActivityMessage()
    class SetError(val place: String, val throwable: Throwable) : MainActivityMessage()
}