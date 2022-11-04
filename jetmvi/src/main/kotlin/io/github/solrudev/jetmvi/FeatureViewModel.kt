package io.github.solrudev.jetmvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * [ViewModel] container for a [Feature].
 *
 * Implements [Flow] of [UiState], so it can be collected to receive UI state updates.
 */
public abstract class FeatureViewModel<in E : Event, out S : UiState>(
	private val feature: Feature<E, S>
) : ViewModel(), Flow<S> {

	init {
		feature.launchIn(viewModelScope)
	}

	final override suspend fun collect(collector: FlowCollector<S>) {
		feature.collect(collector)
	}

	/**
	 * Dispatch a UI event.
	 */
	public fun dispatchEvent(event: E): Boolean = feature.dispatchEvent(event)
}