package com.github.jcornaz.islands

import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.lifecycle.MemoizedValue
import org.spekframework.spek2.style.specification.Suite
import kotlin.test.assertFailsWith

fun Suite.beforeEachTestBlocking(block: suspend () -> Unit) {
    beforeEachTest { runBlocking { block() } }
}

inline fun <reified T : Throwable> assertFailsBlocking(crossinline block: suspend () -> Unit) {
    assertFailsWith<T> { runBlocking { block() } }
}

fun <T> LifecycleAware.memoizedBlocking(mode: CachingMode = defaultCachingMode, destructor: (T) -> Unit = {}, factory: suspend () -> T): MemoizedValue<T> =
    memoized(mode = mode, factory = { runBlocking { factory() } }, destructor = destructor)

fun <T : AutoCloseable> LifecycleAware.memoizedClosable(mode: CachingMode = defaultCachingMode, factory: () -> T): MemoizedValue<T> =
    memoized(mode = mode, factory = factory, destructor = { runCatching { it.close() } })
