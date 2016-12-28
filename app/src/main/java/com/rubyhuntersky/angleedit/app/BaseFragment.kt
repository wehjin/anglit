package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.support.v4.app.Fragment
import com.crashlytics.android.Crashlytics
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
import io.fabric.sdk.android.Fabric
import rx.Observable
import rx.subjects.PublishSubject

/**
 * @author Jeffrey Yu
 * @since 11/27/16.
 */

open class BaseFragment : Fragment() {
    private val lifecycleSubject = PublishSubject.create<FragmentLifecycleMessage>()

    val lifecycleMessages: Observable<FragmentLifecycleMessage>
        get() = lifecycleSubject.asObservable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(context, Crashlytics())
    }

    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleSubject.onNext(ActivityCreated(savedInstanceState))
    }

    final override fun onSaveInstanceState(outState: Bundle) {
        lifecycleSubject.onNext(SaveInstanceState(outState))
        super.onSaveInstanceState(outState)
    }

    final override fun onStart() {
        super.onStart()
        lifecycleSubject.onNext(Start)
    }

    final override fun onStop() {
        lifecycleSubject.onNext(Stop)
        super.onStop()
    }

    final override fun onResume() {
        super.onResume()
        lifecycleSubject.onNext(Resume())
    }

    final override fun onPause() {
        lifecycleSubject.onNext(Pause())
        super.onPause()
    }
}