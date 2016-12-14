package com.rubyhuntersky.angleedit.app

import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.rubyhuntersky.angleedit.app.tools.errorDialog

/**
 * @author Jeffrey Yu
 * @since 12/11/16.
 */

open class BaseActivity : AppCompatActivity() {

    protected fun showError(place: String, t: Throwable) {
        Log.e(this.javaClass.simpleName, place, t)
        errorDialog(this, place, t).show()
    }
}