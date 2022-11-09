package io.github.solrudev.jetmvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal fun <S : JetState> Flow<S>.bind(
	parentView: JetView<S>,
	derivedViews: Array<out JetView<S>>,
	scope: CoroutineScope,
	lifecycle: Lifecycle,
	bindParent: Boolean = true
): Job = scope.launch {
	if (bindParent) {
		renderWithLifecycle(parentView, lifecycle).launchIn(this)
	}
	derivedViews.forEach { derivedView ->
		renderWithLifecycle(derivedView, lifecycle).launchIn(this)
	}
}

private fun <S : JetState> Flow<S>.renderWithLifecycle(jetView: JetView<S>, lifecycle: Lifecycle): Flow<S> {
	if (jetView.trackedState === SKIP_RENDER) {
		return emptyFlow()
	}
	return flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
		.distinctUntilChangedByKeys(jetView.trackedState)
		.onEach(jetView::render)
}