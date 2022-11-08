package io.github.solrudev.jetmvi

/**
 * A view which can render UI state object.
 */
public interface JetView<in S : JetState> {

	/**
	 * Properties of [JetState] whose changes are tracked by this view. When this list is empty (it is by default),
	 * [render] method will be called on every UI state update. Override this to skip render when unrelated properties
	 * change (may be useful in [derived views][derivedView]).
	 *
	 * Example:
	 * ```
	 * override val trackedState = listOf(MyUiState::isButtonEnabled, MyUiState::buttonText)
	 * ```
	 */
	public val trackedState: List<(S) -> Any?>
		get() = emptyList()

	/**
	 * Called when UI state changes.
	 */
	public fun render(uiState: S)
}

/**
 * [JetView] which only hosts [derived views][derivedView] and doesn't render anything itself.
 *
 * [Binding][bind] this view is a no-op (unless [trackedState] is overridden).
 */
public interface HostJetView<in S : JetState> : JetView<S> {

	override val trackedState: List<(S) -> Any?>
		get() = SKIP_RENDER

	override fun render(uiState: S) {}
}

/**
 * For use as a value of [HostJetView.trackedState]. Denotes that rendering for the view should be skipped.
 */
internal val SKIP_RENDER: List<(JetState) -> Any?> = listOf { 0 }