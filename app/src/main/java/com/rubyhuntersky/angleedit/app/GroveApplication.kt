package com.rubyhuntersky.angleedit.app

import DocumentCenter
import android.app.Application
import android.content.Context

/**
 * @author Jeffrey Yu
 * @since 12/10/16.
 */
class GroveApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        RecentSources.enable(this as Context)
        DocumentCenter.enable(this as Context)
    }

    companion object {
        val TAG: String = GroveApplication::class.java.simpleName
    }
}