package io.github.solrudev.jetmvi

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render view each time new state is
 * emitted.
 *
 * Binding by using [jetViewModels] delegate is preferred to manually calling this function, because it correctly
 * manages binding lifecycle.
 * @param jetView a [JetView] to bind UI state flow to, parent [JetView] for [derivedViews].
 * @param derivedViews views derived from [jetView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bind(jetView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : ComponentActivity {
	return bind(jetView, derivedViews, jetView.lifecycleScope, jetView.lifecycle)
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render _only_ derived views each time
 * new state is emitted.
 *
 * Binding by using [jetViewModels] delegate is preferred to manually calling this function, because it correctly
 * manages binding lifecycle.
 * @param parentView parent [JetView].
 * @param derivedViews views derived from [parentView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindDerived(parentView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : ComponentActivity {
	return bind(parentView, derivedViews, parentView.lifecycleScope, parentView.lifecycle, bindParent = false)
}