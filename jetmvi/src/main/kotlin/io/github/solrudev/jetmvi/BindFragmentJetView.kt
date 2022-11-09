package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render [jetView] each time new state is
 * emitted.
 *
 * Binding by using [jetViewModels] delegate is preferred to manually calling this function, because it automatically
 * determines necessary [Lifecycle] to launch coroutines with and correctly manages binding lifecycle.
 *
 * **Do not use in non-UI fragments, as it assumes that fragment's view is not null. Use [bindHeadless] instead.**
 * @param jetView a [JetView] to bind UI state flow to, parent [JetView] for [derivedViews].
 * @param derivedViews views derived from [jetView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bind(jetView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : Fragment {
	return bind(
		jetView,
		derivedViews,
		jetView.viewLifecycleOwner.lifecycleScope,
		jetView.viewLifecycleOwner.lifecycle
	)
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] which will re-render _only_ derived views each time
 * new state is emitted.
 *
 * Binding by using [jetViewModels] delegate is preferred to manually calling this function, because it automatically
 * determines necessary [Lifecycle] to launch coroutines with and correctly manages binding lifecycle.
 *
 * **Do not use in non-UI fragments, as it assumes that fragment's view is not null. Use [bindDerivedHeadless]
 * instead.**
 * @param parentView parent [JetView].
 * @param derivedViews views derived from [parentView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindDerived(parentView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : Fragment {
	return bind(
		parentView,
		derivedViews,
		parentView.viewLifecycleOwner.lifecycleScope,
		parentView.viewLifecycleOwner.lifecycle,
		bindParent = false
	)
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] for non-UI fragment which will re-render it each time
 * new state is emitted.
 *
 * Binding by using [jetViewModels] delegate is preferred to manually calling this function, because it automatically
 * determines necessary [Lifecycle] to launch coroutines with and correctly manages binding lifecycle.
 *
 * **Use only in non-UI fragments, as it doesn't respect fragment's view lifecycle. Use [bind] for fragments with a
 * view.**
 * @param jetView a [JetView] to bind UI state flow to.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindHeadless(jetView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : Fragment {
	return bind(jetView, derivedViews, jetView.lifecycleScope, jetView.lifecycle)
}

/**
 * Launches lifecycle-aware collection of the [Flow] of [JetState] for non-UI fragment which will re-render _only_
 * derived views each time new state is emitted.
 *
 * Binding by using [jetViewModels] delegate is preferred to manually calling this function, because it automatically
 * determines necessary [Lifecycle] to launch coroutines with and correctly manages binding lifecycle.
 *
 * **Use only in non-UI fragments, as it doesn't respect fragment's view lifecycle. Use [bindDerived] for fragments with
 * a view.**
 * @param parentView parent [JetView].
 * @param derivedViews views derived from [parentView]. Created with [derivedView] delegate.
 * @return [Job] of the flow collection.
 */
public fun <S : JetState, V> Flow<S>.bindDerivedHeadless(parentView: V, vararg derivedViews: JetView<S>): Job
		where V : JetView<S>,
			  V : Fragment {
	return bind(parentView, derivedViews, parentView.lifecycleScope, parentView.lifecycle, bindParent = false)
}