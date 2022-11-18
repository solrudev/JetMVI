package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Wraps a [Feature] and maps its event and UI state types using provided mappers.
 * @param eventMapper function which transforms from dispatched events type to feature's event type.
 * @param stateMapper function which transforms from feature's UI state type to desired UI state
 */
public fun <InEvent : JetEvent, InState : JetState, OutEvent : JetEvent, OutState : JetState> Feature<InEvent, InState>.wrap(
	eventMapper: (OutEvent) -> InEvent?,
	stateMapper: (InState) -> OutState
): Feature<OutEvent, OutState> {
	return AdapterFeature(this, eventMapper, stateMapper)
}

/**
 * Wraps a [Feature] and maps its event type using provided mapper.
 * @param eventMapper function which transforms from dispatched events type to feature's event type.
 */
@JvmName("wrapEvent")
public fun <InEvent : JetEvent, OutEvent : JetEvent, State : JetState> Feature<InEvent, State>.wrap(
	eventMapper: (OutEvent) -> InEvent?
): Feature<OutEvent, State> {
	return AdapterFeature(this, eventMapper, stateMapper = { it })
}

/**
 * Wraps a [Feature] and maps its UI state type using provided mapper.
 * @param stateMapper function which transforms from feature's UI state type to desired UI state
 */
@JvmName("wrapState")
public fun <Event : JetEvent, InState : JetState, OutState : JetState> Feature<Event, InState>.wrap(
	stateMapper: (InState) -> OutState
): Feature<Event, OutState> {
	return AdapterFeature(this, eventMapper = { it }, stateMapper)
}

/**
 * Implementation of [Feature] which receives an [Feature] and maps its event and UI state types using provided mappers.
 *
 * @param feature [Feature] to wrap.
 * @param eventMapper function which transforms from dispatched events type to [feature's][feature] event type.
 * @param stateMapper function which transforms from [feature's][feature] UI state type to desired UI state
 * type.
 */
private class AdapterFeature<in InEvent : JetEvent, in InState : JetState, in OutEvent : JetEvent, out OutState : JetState>(
	private val feature: Feature<InEvent, InState>,
	private val eventMapper: (OutEvent) -> InEvent?,
	private val stateMapper: (InState) -> OutState,
) : Feature<OutEvent, OutState>, Flow<OutState> by feature.map(stateMapper) {

	override fun launchIn(scope: CoroutineScope): Job = feature.launchIn(scope)

	override fun dispatchEvent(event: OutEvent): Boolean {
		return eventMapper(event)?.let(feature::dispatchEvent) ?: true
	}
}