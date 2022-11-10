package io.github.solrudev.jetmvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Launches views rendering of this [JetState] flow in the given [scope] with [lifecycle].
 */
internal fun <S : JetState> Flow<S>.bind(
	parentView: JetView<S>,
	derivedViews: Array<out JetView<S>>,
	scope: CoroutineScope,
	lifecycle: Lifecycle,
	bindParent: Boolean = true
): Job = scope.launch {
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