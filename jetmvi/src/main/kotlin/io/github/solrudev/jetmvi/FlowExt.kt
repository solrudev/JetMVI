package io.github.solrudev.jetmvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * Returns flow where all subsequent repetitions of the same keys are filtered out, where keys are extracted with
 * [keySelectors] functions.
 */
internal fun <T> Flow<T>.distinctUntilChangedByKeys(keySelectors: List<(T) -> Any?>): Flow<T> {
	if (keySelectors.isEmpty()) {
		return this
	}
	// unsafe flow as we don't change context
	return object : Flow<T> {
		override suspend fun collect(collector: FlowCollector<T>) {
			var previousKeys: Array<Any?>? = null
			this@distinctUntilChangedByKeys.collect { value ->
				val keys = Array(keySelectors.size) { index -> keySelectors[index](value) }
				if (previousKeys == null || !keysAreEqual(keys, previousKeys!!)) {
					previousKeys = keys
					collector.emit(value)
				}
			}
		}
	}
}

// skipping unnecessary bounds checking
@Suppress("NOTHING_TO_INLINE")
private inline fun keysAreEqual(keys1: Array<Any?>, keys2: Array<Any?>): Boolean {
	keys1.forEachIndexed { index, key ->
		if (key != keys2[index]) {
			return false
		}
	}
	return true
}