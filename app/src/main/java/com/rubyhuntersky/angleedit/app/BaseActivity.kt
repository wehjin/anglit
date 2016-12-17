package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.rubyhuntersky.angleedit.app.tools.errorDialog

/**
 * @author Jeffrey Yu
 * @since 12/11/16.
 */

open class BaseActivity : AppCompatActivity() {

    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(tag, "onCreate $savedInstanceState")
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        Log.v(tag, "onDestroy")
        super.onDestroy()
    }

    override fun onResume() {
        Log.v(tag, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.v(tag, "onPause")
        super.onPause()
    }

    override fun finishAffinity() {
        Log.v(tag, "finishAffinity")
        super.finishAffinity()
    }

    override fun finish() {
        Log.v(tag, "finish")
        super.finish()
    }

    protected fun showError(place: String, t: Throwable) {
        Log.e(this.javaClass.simpleName, place, t)
        errorDialog(this, place, t).show()
    }
}