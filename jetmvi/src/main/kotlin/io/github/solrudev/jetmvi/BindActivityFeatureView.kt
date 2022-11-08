package io.github.solrudev.jetmvi

import androidx.activity.ComponentActivity
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
			  V : ComponentActivity {
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
			  V : ComponentActivity {
	if (derivedView.trackedState === SKIP_RENDER) {
		return Job()
	}
	return parentView.lifecycleScope.launch {
		parentView.repeatOnLifecycle(Lifecycle.State.STARTED) {
			distinctUntilChangedByKeys(derivedView.trackedState).collect(derivedView::render)
		}
	}
}