package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.support.v4.app.Fragment
import com.rubyhuntersky.angleedit.app.FragmentLifecycleMessage.*
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

    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleSubject.onNext(ActivityCreated(savedInstanceState))
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