package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Implementation of [Feature] which receives an [AssemblyFeature] and maps its event and UI state types using provided
 * [EventMapper] and [UiStateMapper].
 */
public open class AdapterFeature<in InEvent : Event, in InUiState : UiState, in OutEvent : Event, out OutUiState : UiState>(
	private val feature: AssemblyFeature<InEvent, InUiState>,
	private val eventMapper: EventMapper<InEvent, OutEvent>,
	private val uiStateMapper: UiStateMapper<InUiState, OutUiState>
) : Feature<OutEvent, OutUiState> {

	private val events = MutableSharedFlow<OutEvent>(
		extraBufferCapacity = 16,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	private val uiState = MutableStateFlow(uiStateMapper(feature.initialUiState))

	final override suspend fun collect(collector: FlowCollector<OutUiState>): Nothing {
		uiState.collect(collector)
	}

	final override fun launchIn(scope: CoroutineScope): Job = scope.launch {
		feature.events
			.combine(feature, feature.reducer::reduce)
			.map(uiStateMapper)
			.onEach(uiState::emit)
			.launchIn(this)
		feature.middlewares
			.map { it.apply(feature.events) }
			.merge()
			.map(eventMapper)
			.onEach(events::emit)
			.launchIn(this)
	}

	final override fun dispatchEvent(event: OutEvent): Boolean = events.tryEmit(event)
}