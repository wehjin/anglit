package com.rubyhuntersky.angleedit.app

import android.net.Uri
import android.os.Parcel
import com.rubyhuntersky.angleedit.app.tools.BaseParcelable
import com.rubyhuntersky.angleedit.app.tools.read
import com.rubyhuntersky.angleedit.app.tools.write

/**
 * @author Jeffrey Yu
 * @since 12/9/16.
 */

data class MainActivityModel(var sourceUri: Uri?, var documentId: String?) : BaseParcelable {
    override fun writeToParcel(outState: Parcel, flags: Int) {
        outState.write(sourceUri, documentId)
    }

    companion object {
        @JvmField val CREATOR = BaseParcelable.generateCreator { MainActivityModel(it.read(), it.read()) }
    }
}
