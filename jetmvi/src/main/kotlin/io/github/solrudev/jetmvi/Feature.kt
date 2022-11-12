package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * An object which represents a single complete feature of an app.
 *
 * Implements [Flow] of [JetState], so it can be collected to receive UI state updates.
 *
 * Has [AssemblyFeature] and [AdapterFeature] implementations.
 */
public sealed interface Feature<in E : JetEvent, out S : JetState> : Flow<S> {

	/**
	 * Launches the feature in the given coroutine scope.
	 * @return [Job] of the feature.
	 */
	public fun launchIn(scope: CoroutineScope): Job

	/**
	 * Dispatch an event.
	 * @return whether the event was emitted successfully.
	 */
	public fun dispatchEvent(event: E): Boolean
}

/**
 * Standard implementation of [Feature] which assembles [Middlewares][Middleware] and [Reducer] together.
 */
public open class AssemblyFeature<in E : JetEvent, out S : JetState> private constructor(
	private val middlewares: List<Middleware<E>>,
	private val reducer: Reducer<E, S>,
	private val uiState: MutableStateFlow<S>
) : Feature<E, S>, Flow<S> by uiState {

	public constructor(
		middlewares: List<Middleware<E>> = emptyList(),
		reducer: Reducer<E, S>,
		initialUiState: S
	) : this(middlewares, reducer, MutableStateFlow(initialUiState))

	private val events = MutableSharedFlow<E>(
		extraBufferCapacity = 16,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	final override fun launchIn(scope: CoroutineScope): Job = scope.launch {
		events
			.combine(uiState, reducer::reduce)
			.onEach(uiState::emit)
			.launchIn(this)
		middlewares
			.map { it.apply(events) }
			.merge()
			.onEach(events::emit)
			.launchIn(this)
	}

	final override fun dispatchEvent(event: E): Boolean = events.tryEmit(event)
}