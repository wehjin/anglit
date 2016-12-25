package com.rubyhuntersky.angleedit.app

import android.app.Application
import android.content.Context
import com.rubyhuntersky.angleedit.app.data.AccentCenter
import com.rubyhuntersky.angleedit.app.data.DocumentCenter
import com.rubyhuntersky.angleedit.app.data.TitleCenter

/**
 * @author Jeffrey Yu
 * @since 12/10/16.
 */
class GroveApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AccentCenter.enable(this as Context)
        RecentSources.enable(this as Context)
        DocumentCenter.enable(this as Context)
        TitleCenter.enablePersistence(this as Context)
    }

    companion object {
        val TAG: String = GroveApplication::class.java.simpleName
    }
}