package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render view each time
 * new state is emitted.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bind(jetView: V): Job
		where V : JetView<S>,
			  V : Fragment {
	if (jetView.trackedState === SKIP_RENDER) {
		return Job()
	}
	return jetView.viewLifecycleOwner.lifecycleScope.launch {
		jetView.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(jetView.trackedState).collect(jetView::render)
		}
	}
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] for non-UI fragment which will re-render it each
 * time new state is emitted.
 *
 * **Use only in non-UI fragments, as it doesn't respect fragment's view lifecycle.**
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindHeadless(jetView: V): Job
		where V : JetView<S>,
			  V : Fragment {
	if (jetView.trackedState === SKIP_RENDER) {
		return Job()
	}
	return jetView.lifecycleScope.launch {
		jetView.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(jetView.trackedState).collect(jetView::render)
		}
	}
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render derived view each time
 * new state is emitted.
 * @param parentView parent [JetView].
 * @param derivedView [JetView] derived from [parentView]. Created with `derivedView` delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindDerived(parentView: V, derivedView: JetView<S>): Job
		where V : JetView<S>,
			  V : Fragment {
	if (derivedView.trackedState === SKIP_RENDER) {
		return Job()
	}
	return parentView.viewLifecycleOwner.lifecycleScope.launch {
		parentView.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(derivedView.trackedState).collect(derivedView::render)
		}
	}
}