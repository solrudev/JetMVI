package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

/**
 * Implementation of [Feature] which receives an [AssemblyFeature] and maps its event and UI state types using provided
 * [EventMappers][EventMapper] and [UiStateMapper].
 *
 * @param feature [AssemblyFeature] to transform.
 * @param eventMapper [EventMapper] which transforms from dispatched events type to [feature's][feature] event type.
 * @param uiStateMapper [UiStateMapper] which transforms from [feature's][feature] UI state type to desired UI state
 * type.
 * @param mappedEventToIgnore [class instance][KClass] of marker interface, events marked with it should be ignored by
 * [feature]. This interface can be extending [MappedEventToIgnore], so it's possible to mark events to be ignored by
 * one feature, but not the other. By default, all events are dispatched to the feature.
 */
public open class AdapterFeature<in InEvent : Event, in InUiState : UiState, in OutEvent : Event, out OutUiState : UiState>(
	private val feature: AssemblyFeature<InEvent, InUiState>,
	private val eventMapper: EventMapper<OutEvent, InEvent>,
	private val uiStateMapper: UiStateMapper<InUiState, OutUiState>,
	private val mappedEventToIgnore: KClass<out MappedEventToIgnore> = DispatchAllEvents::class
) : Feature<OutEvent, OutUiState> {

	private val uiState = feature.map(uiStateMapper)

	final override suspend fun collect(collector: FlowCollector<OutUiState>) {
		uiState.collect(collector)
	}

	final override fun launchIn(scope: CoroutineScope): Job = feature.launchIn(scope)

	final override fun dispatchEvent(event: OutEvent): Boolean {
		if (mappedEventToIgnore.isInstance(event)) {
			return true
		}
		return feature.dispatchEvent(eventMapper(event))
	}
}

private object DispatchAllEvents : MappedEventToIgnore