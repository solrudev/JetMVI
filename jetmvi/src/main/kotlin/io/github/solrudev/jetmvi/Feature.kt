package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * An object which represents a single complete feature of an app.
 *
 * Implements [Flow] of [UiState], so it can be collected to receive UI state updates.
 */
public sealed interface Feature<in E : Event, out S : UiState> : Flow<S> {

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
public open class AssemblyFeature<E : Event, S : UiState>(
	private val middlewares: List<Middleware<E>> = emptyList(),
	private val reducer: Reducer<E, S>,
	internal val initialUiState: S
) : Feature<E, S> {

	private val _events = MutableSharedFlow<E>(
		extraBufferCapacity = 16,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	internal val events = _events.asSharedFlow()
	private val uiState = MutableStateFlow(initialUiState)

	final override suspend fun collect(collector: FlowCollector<S>): Nothing {
		uiState.collect(collector)
	}

	final override fun launchIn(scope: CoroutineScope): Job = scope.launch {
		_events
			.combine(uiState, reducer::reduce)
			.onEach(uiState::emit)
			.launchIn(this)
		middlewares
			.map { it.apply(_events) }
			.merge()
			.onEach(_events::emit)
			.launchIn(this)
	}

	final override fun dispatchEvent(event: E): Boolean = _events.tryEmit(event)
}