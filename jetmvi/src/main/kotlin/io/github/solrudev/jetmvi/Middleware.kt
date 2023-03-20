package io.github.solrudev.jetmvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Allows to perform side effects by intercepting, consuming and producing events inside of a [Feature].
 */
public fun interface Middleware<E : JetEvent> {

	/**
	 * Returns transformed events flow.
	 */
	public fun apply(events: Flow<E>): Flow<E>
}

/**
 * Allows to perform side effects by intercepting, consuming and producing events inside of a [Feature] via
 * [MiddlewareScope].
 *
 * Example:
 * ```
 * val middleware = JetMiddleware { // this: MiddlewareScope = Flow of events + ProducerScope
 *     filterIsInstance<DataProcessingRequested>()
 *         .map { event -> event.payload }
 *         .filter { payload -> payload.isProcessingAllowed }
 *         .map { payload ->
 *             val data = doSomething(payload)
 *             DataProcessed(data, payload.id)
 *         }
 *         .onEach(::send)
 *         .launchIn(this)
 *     // using onEvent() or onEventLatest()
 *     onEvent<DataProcessingRequested> { event ->
 *         if (event.payload.isProcessingAllowed) {
 *             val data = doSomething(event.payload)
 *             send(DataProcessed(data, event.payload.id))
 *         }
 *     }
 *     // it's possible to launch multiple flows with side effects
 *     onEvent<ButtonClicked> { event ->
 *         val newFlagValue = toggleFlag(event.id)
 *         send(FlagToggled(newFlagValue, event.id))
 *     }
 * }
 * ```
 */
public fun interface JetMiddleware<E : JetEvent> : Middleware<E> {

	/**
	 * Sets up side effects and events flow transformations.
	 */
	public fun MiddlewareScope<E>.apply()

	override fun apply(events: Flow<E>): Flow<E> = channelFlow producerScope@{
		with(MiddlewareScope(events, producerScope = this@producerScope)) {
			apply()
		}
	}.buffer(Channel.RENDEZVOUS)
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
	 * flow.
	 *
	 * The difference from [onEvent] is that when the input events flow emits a new event of type [E], [action] block
	 * for the previous event is canceled. It is useful when the event starts to collect some flow that shouldn't have
	 * multiple parallel collectors.
	 * @return [Job] of the coroutine.
	 */
	public inline fun <reified E : JetEvent> onEventLatest(noinline action: suspend (E) -> Unit): Job = launch {
		collectEventLatest(action)
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