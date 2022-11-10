package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render view each time new state is
 * emitted. It also accounts for [JetView.trackedState].
 *
 * Binding by using [jetViewModels] (or [activityJetViewModels]) delegate is preferred to manually calling this
 * function, because it automatically manages binding lifecycle. Don't use this function together with aforementioned
 * delegates to avoid duplicate binding.
 *
 * **For activity:**
 *
 * This function must be called in activity's `onCreate()`.
 *
 * **For fragment with a view:**
 *
 * If fragment has a view, this function must be called in fragment's `onViewCreated()`.
 *
 * **For fragment without a view:**
 *
 * If fragment doesn't have a view, this function must be called in fragment's `onCreate()`.
 *
 * @param jetView a view to bind UI state flow to, parent [JetView] for [derivedViews].
 * @param derivedViews views derived from [jetView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bind(jetView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : LifecycleOwner {
	if (jetView is Fragment && jetView.view != null) {
		return bind(jetView, derivedViews, jetView.viewLifecycleOwner.lifecycle)
	}
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
 * **For activity:**
 *
 * This function must be called in activity's `onCreate()`.
 *
 * **For fragment with a view:**
 *
 * If fragment has a view, this function must be called in fragment's `onViewCreated()`.
 *
 * **For fragment without a view:**
 *
 * If fragment doesn't have a view, this function must be called in fragment's `onCreate()`.
 *
 * @param parentView parent [JetView].
 * @param derivedViews views derived from [parentView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindDerived(parentView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : LifecycleOwner {
	if (parentView is Fragment && parentView.view != null) {
		return bind(parentView, derivedViews, parentView.viewLifecycleOwner.lifecycle, bindParent = false)
	}
	return bind(parentView, derivedViews, parentView.lifecycle, bindParent = false)
}

/**
 * Launches views rendering of this [JetState] flow with the given [lifecycle].
 */
internal fun <S : JetState> Flow<S>.bind(
	parentView: JetView<S>,
	derivedViews: Array<out JetView<S>>,
	lifecycle: Lifecycle,
	bindParent: Boolean = true
): Job = lifecycle.coroutineScope.launch {
	lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
		if (bindParent) {
			renderWith(parentView).launchIn(this)
		}
		derivedViews.forEach { derivedView ->
			renderWith(derivedView).launchIn(this)
		}
	}
}

/**
 * Returns a flow of [JetState] which calls [jetView's][jetView] `render()` on each value (accounting
 * [JetView.trackedState]).
 */
private fun <S : JetState> Flow<S>.renderWith(jetView: JetView<S>): Flow<S> {
	if (jetView.trackedState === SKIP_RENDER) {
		return emptyFlow()
	}
	return distinctUntilChangedByKeys(jetView.trackedState).onEach(jetView::render)
}