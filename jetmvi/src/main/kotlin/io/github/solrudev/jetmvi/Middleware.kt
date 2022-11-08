package io.github.solrudev.jetmvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Allows to intercept events from a [Feature], perform side effects and emit additional events.
 */
public interface Middleware<E : JetEvent> {
	public fun apply(events: Flow<E>): Flow<E>
}

/**
 * Starts events flow collection and emits only specified event into a [collector].
 */
public suspend inline fun <reified E : JetEvent> Flow<JetEvent>.collectEvent(collector: FlowCollector<E>) {
	filterIsInstance<E>().collect(collector)
}