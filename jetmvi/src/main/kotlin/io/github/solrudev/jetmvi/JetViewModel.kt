package io.github.solrudev.jetmvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Only instances of this sealed interface can be obtained from [jetViewModels] delegate.
 *
 * Implemented by [FeatureViewModel] and [UDFViewModel].
 */
public sealed interface JetViewModel<out S : JetState> : Flow<S>

/**
 * [ViewModel] container for a [Feature].
 *
 * Implements [Flow] of [JetState], so it can be collected to receive UI state updates.
 */
public abstract class FeatureViewModel<in E : JetEvent, out S : JetState>(
	private val feature: Feature<E, S>
) : ViewModel(), JetViewModel<S> {

	init {
		feature.launchIn(viewModelScope)
	}

	final override suspend fun collect(collector: FlowCollector<S>) {
		feature.collect(collector)
	}

	/**
	 * Dispatch a UI event.
	 * @return whether the event was emitted successfully.
	 */
	public fun dispatchEvent(event: E): Boolean = feature.dispatchEvent(event)
}

/**
 * [ViewModel] which implements unidirectional data flow pattern.
 *
 * Implements [Flow] of [JetState], so it can be collected to receive UI state updates. Also, thanks to this, it's
 * possible to use any [Flow] operators on `this` inside the view model. For example:
 *
 * ```
 * fun startLogging() {
 *     onEach { uiState -> logger.log(uiState) }.launchIn(viewModelScope)
 *     // or shorter
 *     onEach(logger::log).launchIn(viewModelScope)
 * }
 * ```
 */
public abstract class UDFViewModel<S : JetState>(initialUiState: S) : ViewModel(), JetViewModel<S> {

	private val uiState = MutableStateFlow(initialUiState)

	/**
	 * Returns latest cached UI state.
	 */
	protected val currentState: S
		get() = uiState.value

	final override suspend fun collect(collector: FlowCollector<S>) {
		uiState.collect(collector)
	}

	/**
	 * Atomically updates UI state with the value returned from [reducer].
	 */
	protected fun updateState(reducer: S.() -> S) {
		uiState.update(reducer)
	}
}