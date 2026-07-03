package org.videolan.vlc.util

import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.videolan.tools.SingletonHolder

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

inline fun <reified T> argumentCaptor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)

fun <T> uninitialized(): T = null as T

fun <T, A> SingletonHolder<T, A>.applyMock(mock: T) {
    instance = mock
}
