package io.github.solrudev.jetmvi

import androidx.activity.ComponentActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render view each time new state is
 * emitted. It also accounts for [JetView.trackedState].
 *
 * Binding by using [jetViewModels] (or [activityJetViewModels]) delegate is preferred to manually calling this
 * function, because it automatically manages binding lifecycle. Don't use this function together with aforementioned
 * delegates to avoid duplicate binding.
 *
 * This function must be called in activity's `onCreate()`.
 *
 * @param jetView a view to bind UI state flow to, parent [JetView] for [derivedViews].
 * @param derivedViews views derived from [jetView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bind(jetView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : ComponentActivity {
	return bind(jetView, derivedViews, jetView.lifecycle)
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render _only_ derived views each time
 * new state is emitted. It also accounts for [JetView.trackedState].
 *
 * Binding by using [jetViewModels] (or [activityJetViewModels]) delegate is preferred to manually calling this
 * function, because it automatically manages binding lifecycle. Don't use this function together with aforementioned
 * delegates to avoid duplicate binding.
 *
 * This function must be called in activity's `onCreate()`.
 *
 * @param parentView parent [JetView].
 * @param derivedViews views derived from [parentView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindDerived(parentView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : ComponentActivity {
	return bind(parentView, derivedViews, parentView.lifecycle, bindParent = false)
}