package org.videolan.vlc.util

import androidx.lifecycle.LiveData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <T> getValue(liveData: LiveData<T>): T {
    val data = arrayOfNulls<Any>(1)
    val latch = CountDownLatch(1)
    liveData.observeForever {
        data[0] = it
        latch.countDown()
    }
    if (!latch.await(2, TimeUnit.SECONDS)) {
        error("LiveData value was never set")
    }
    @Suppress("UNCHECKED_CAST")
    return data[0] as T
}
