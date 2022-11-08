package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map

/**
 * Implementation of [Feature] which receives an [AssemblyFeature] and maps its event and UI state types using provided
 * [EventMapper] and [UiStateMapper].
 *
 * @param feature [AssemblyFeature] to transform.
 * @param eventMapper [EventMapper] which transforms from dispatched events type to [feature's][feature] event type.
 * @param uiStateMapper [UiStateMapper] which transforms from [feature's][feature] UI state type to desired UI state
 * type.
 */
public open class AdapterFeature<in InEvent : Event, in InUiState : UiState, in OutEvent : Event, out OutUiState : UiState>(
	private val feature: AssemblyFeature<InEvent, InUiState>,
	private val eventMapper: EventMapper<OutEvent, InEvent?>,
	private val uiStateMapper: UiStateMapper<InUiState, OutUiState>,
) : Feature<OutEvent, OutUiState> {

	private val uiState = feature.map(uiStateMapper)

	final override suspend fun collect(collector: FlowCollector<OutUiState>) {
		uiState.collect(collector)
	}

	final override fun launchIn(scope: CoroutineScope): Job = feature.launchIn(scope)

	final override fun dispatchEvent(event: OutEvent): Boolean {
		return eventMapper(event)?.let(feature::dispatchEvent) ?: true
	}
}