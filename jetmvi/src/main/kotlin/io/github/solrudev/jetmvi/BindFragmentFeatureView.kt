package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Launches lifecycle-aware collection of the [Flow] of [UiState] which will re-render view each time
 * new state is emitted.
 * @return [Job] of the flow collection.
 */
public fun <S : UiState, V> Flow<S>.bind(featureView: V): Job
		where V : FeatureView<S>,
			  V : Fragment {
	if (featureView.trackedUiState === SKIP_RENDER) {
		return Job()
	}
	return featureView.viewLifecycleOwner.lifecycleScope.launch {
		featureView.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(featureView.trackedUiState).collect(featureView::render)
		}
	}
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [UiState] for non-UI fragment which will re-render it each
 * time new state is emitted.
 *
 * **Use only in non-UI fragments, as it doesn't respect fragment's view lifecycle.**
 * @return [Job] of the flow collection.
 */
public fun <S : UiState, V> Flow<S>.bindHeadless(featureView: V): Job
		where V : FeatureView<S>,
			  V : Fragment {
	if (featureView.trackedUiState === SKIP_RENDER) {
		return Job()
	}
	return featureView.lifecycleScope.launch {
		featureView.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(featureView.trackedUiState).collect(featureView::render)
		}
	}
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [UiState] which will re-render derived view each time
 * new state is emitted.
 * @param parentView parent [FeatureView].
 * @param derivedView [FeatureView] derived from [parentView]. Created with `derivedView` delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : UiState, V> Flow<S>.bindDerived(parentView: V, derivedView: FeatureView<S>): Job
		where V : FeatureView<S>,
			  V : Fragment {
	if (derivedView.trackedUiState === SKIP_RENDER) {
		return Job()
	}
	return parentView.viewLifecycleOwner.lifecycleScope.launch {
		parentView.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(derivedView.trackedUiState).collect(derivedView::render)
		}
	}
}