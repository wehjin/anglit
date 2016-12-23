package com.rubyhuntersky.angleedit.app

import android.os.Bundle

/**
 * @author Jeffrey Yu
 * @since 11/27/16.
 */

sealed class FragmentLifecycleMessage {
    class ActivityCreated(val savedState: Bundle?) : FragmentLifecycleMessage()
    class Resume() : FragmentLifecycleMessage()
    class Pause() : FragmentLifecycleMessage()
    object Start : FragmentLifecycleMessage()
    object Stop : FragmentLifecycleMessage()
}