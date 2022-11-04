package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Implementation of [Feature] which receives an [AssemblyFeature] and maps its event and UI state types using provided
 * [EventMappers][EventMapper] and [UiStateMapper].
 *
 * @param feature [AssemblyFeature] to transform.
 * @param toEventMapper [EventMapper] which transforms from [feature's][feature] event type to desired event type.
 * @param fromEventMapper [EventMapper] which transforms from dispatched events type to [feature's][feature] event
 * type.
 * @param uiStateMapper [UiStateMapper] which transforms from [feature's][feature] UI state type to desired UI state
 * type.
 */
public open class AdapterFeature<in InEvent : Event, in InUiState : UiState, in OutEvent : Event, out OutUiState : UiState>(
	private val feature: AssemblyFeature<InEvent, InUiState>,
	private val toEventMapper: EventMapper<InEvent, OutEvent>,
	private val fromEventMapper: EventMapper<OutEvent, InEvent>,
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
		feature
			.map(uiStateMapper)
			.onEach(uiState::emit)
			.launchIn(this)
		feature.events
			.map(toEventMapper)
			.onEach(events::emit)
			.launchIn(this)
		feature.launchIn(this)
	}

	final override fun dispatchEvent(event: OutEvent): Boolean = feature.dispatchEvent(fromEventMapper(event))
}