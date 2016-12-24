package com.rubyhuntersky.angleedit.app.tools

/**
 * @author Jeffrey Yu
 * @since 12/24/16.
 */

fun Throwable.toErrorMessage(place: String): String {
    return "$place \u2014 ${javaClass.simpleName}\n$message"
}
 
