package io.github.solrudev.jetmvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Allows to intercept events from a [Feature], perform side effects and emit additional events.
 */
public fun interface Middleware<E : JetEvent> {

	/**
	 * Applies this middleware to an events flow.
	 */
	public fun apply(events: Flow<E>): Flow<E>
}

/**
 * Allows events to be intercepted from a [Feature], consumed and produced via [MiddlewareScope].
 *
 * Example:
 * ```
 * val middleware = JetMiddleware { // this: MiddlewareScope<E>
 *     launch {
 *         filterIsInstance<DataProcessingRequested>()
 *             .map { event -> event.payload }
 *             .filter { payload -> payload.isProcessingAllowed }
 *             .map { payload ->
 *                 val data = doSomething(payload)
 *                 DataProcessed(data, payload.id)
 *             }
 *             .onEach(::send)
 *     }
 *     // or using onEvent()
 *     onEvent<DataProcessingRequested> { event ->
 *         if (event.payload.isProcessingAllowed) {
 *             val data = doSomething(event.payload)
 *             send(DataProcessed(data, event.payload.id))
 *         }
 *     }
 *     // it's possible to launch multiple flows with side effects
 *     onEvent<ButtonClicked> { event ->
 *         val newFlagValue = toggleFlag()
 *         send(FlagToggled(newFlagValue))
 *     }
 * }
 * ```
 */
public fun interface JetMiddleware<E : JetEvent> : Middleware<E> {

	/**
	 * Applies this middleware to an events flow.
	 */
	public fun MiddlewareScope<E>.apply()

	override fun apply(events: Flow<E>): Flow<E> = channelFlow producerScope@{
		with(MiddlewareScope(events, producerScope = this@producerScope)) {
			apply()
		}
	}
}

/**
 * Scope for [JetMiddleware]. Implements both [Flow] and [ProducerScope].
 */
public class MiddlewareScope<E : JetEvent> internal constructor(
	events: Flow<E>,
	producerScope: ProducerScope<E>
) : Flow<E> by events, ProducerScope<E> by producerScope {

	/**
	 * Launches a new coroutine which emits only events of type [E] into a [collector]. Input events flow is
	 * [conflated][conflate].
	 * @return [Job] of the coroutine.
	 */
	public inline fun <reified E : JetEvent> onEvent(collector: FlowCollector<E>): Job = launch {
		conflate().collectEvent(collector)
	}

	/**
	 * Launches a new coroutine which invokes [action] every time an event of type [E] is emitted from input events
	 * flow. Input events flow is [conflated][conflate].
	 *
	 * The difference from [onEvent] is that when the input events flow emits a new event of type [E], [action] block
	 * for the previous event is canceled.
	 * @return [Job] of the coroutine.
	 */
	public inline fun <reified E : JetEvent> onEventLatest(noinline action: suspend (E) -> Unit): Job = launch {
		conflate().collectEventLatest(action)
	}
}

/**
 * Starts events flow collection and emits only events of type [E] into a [collector].
 */
public suspend inline fun <reified E : JetEvent> Flow<JetEvent>.collectEvent(collector: FlowCollector<E>) {
	filterIsInstance<E>().collect(collector)
}

/**
 * Terminal flow operator which collects only events of type [E] from events flow with provided [action].
 *
 * The difference from [collectEvent] is that when the events flow emits a new event of type [E], [action] block for the
 * previous event is canceled.
 */
public suspend inline fun <reified E : JetEvent> Flow<JetEvent>.collectEventLatest(noinline action: suspend (E) -> Unit) {
	filterIsInstance<E>().collectLatest(action)
}