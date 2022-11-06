package io.github.solrudev.jetmvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal fun <T> Flow<T>.distinctUntilChangedByKeys(keySelectors: List<(T) -> Any?>): Flow<T> {
	if (keySelectors.isEmpty()) {
		return this
	}

	// skipping unnecessary lengths checks
	fun keysAreEqual(keys1: Array<Any?>, keys2: Array<Any?>): Boolean {
		keys1.forEachIndexed { index, key ->
			if (key != keys2[index]) {
				return false
			}
		}
		return true
	}

	var previousKeys: Array<Any?>? = null
	return flow {
		collect { value ->
			val keys = Array(keySelectors.size) { index -> keySelectors[index](value) }
			if (previousKeys == null || !keysAreEqual(keys, previousKeys!!)) {
				previousKeys = keys
				emit(value)
			}
		}
	}
}