package com.rubyhuntersky.angleedit.app.tools

import android.net.Uri

/**
 * @author Jeffrey Yu
 * @since 7/30/16.
 */

val String.isHttpUrl: Boolean get() = startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
val String.asHttpUri: Uri? get() = if (isHttpUrl) Uri.parse(this) else null
