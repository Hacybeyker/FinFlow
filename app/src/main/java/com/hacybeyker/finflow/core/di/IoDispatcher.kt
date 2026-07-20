package com.hacybeyker.finflow.core.di

import javax.inject.Qualifier

/** Marks the injectable [kotlinx.coroutines.CoroutineDispatcher] for IO-bound work, so it can be
 * swapped for a `TestDispatcher` instead of hardcoding [kotlinx.coroutines.Dispatchers.IO]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
