package com.rubyhuntersky.angleedit.app.tools

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

val String.isHttpUrl: Boolean get() {
    return startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
}
