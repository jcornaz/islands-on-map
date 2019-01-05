package com.github.jcornaz.islands.test

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.lifecycle.MemoizedValue
import org.spekframework.spek2.style.specification.Suite

fun Suite.beforeEachBlocking(block: suspend () -> Unit) {
    beforeEach { runBlocking { block() } }
}

fun <T> LifecycleAware.memoizedBlocking(mode: CachingMode = defaultCachingMode, destructor: (T) -> Unit = {}, factory: suspend () -> T): MemoizedValue<T> =
    memoized(mode = mode, factory = { runBlocking { factory() } }, destructor = destructor)

fun <T : AutoCloseable> LifecycleAware.memoizedClosable(mode: CachingMode = defaultCachingMode, factory: () -> T): MemoizedValue<T> =
    memoized(mode = mode, factory = factory, destructor = { runCatching { it.close() } })

inline fun <reified T : Any> LifecycleAware.memoizedMock(
    relaxed: Boolean = false,
    relaxUnitFun: Boolean = false,
    crossinline setup: T.() -> Unit = {}
): MemoizedValue<T> =
    memoized { mockk(relaxed = relaxed, relaxUnitFun = relaxUnitFun, block = setup) }
