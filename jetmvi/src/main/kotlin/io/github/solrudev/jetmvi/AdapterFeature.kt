package io.github.solrudev.jetmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of [Feature] which receives an [AssemblyFeature] and maps its event and UI state types using provided
 * [JetEventMapper] and [JetStateMapper].
 *
 * @param feature [AssemblyFeature] to transform.
 * @param eventMapper [JetEventMapper] which transforms from dispatched events type to [feature's][feature] event type.
 * @param stateMapper [JetStateMapper] which transforms from [feature's][feature] UI state type to desired UI state
 * type.
 */
public open class AdapterFeature<in InEvent : JetEvent, in InState : JetState, in OutEvent : JetEvent, out OutState : JetState>(
	private val feature: AssemblyFeature<InEvent, InState>,
	private val eventMapper: JetEventMapper<OutEvent, InEvent?>,
	stateMapper: JetStateMapper<InState, OutState>,
) : Feature<OutEvent, OutState>, Flow<OutState> by feature.map(stateMapper) {

	final override fun launchIn(scope: CoroutineScope): Job = feature.launchIn(scope)

	final override fun dispatchEvent(event: OutEvent): Boolean {
		return eventMapper(event)?.let(feature::dispatchEvent) ?: true
	}
}