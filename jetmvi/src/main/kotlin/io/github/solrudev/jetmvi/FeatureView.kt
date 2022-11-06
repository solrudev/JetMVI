package io.github.solrudev.jetmvi

/**
 * A view which can render UI state object.
 */
public interface FeatureView<in S : UiState> {

	/**
	 * Properties of [UiState] whose changes are tracked by this view. When this list is empty (it is by default),
	 * [render] method will be called on every UI state update. Override this to skip render when unrelated properties
	 * change (may be useful in [derived views][derivedView]).
	 *
	 * Example:
	 * ```
	 * override val trackedUiState = listOf(MyUiState::isButtonEnabled, MyUiState::buttonText)
	 * ```
	 */
	public val trackedUiState: List<(S) -> Any?>
		get() = emptyList()

	/**
	 * Called when UI state changes.
	 */
	public fun render(uiState: S)
}

/**
 * [FeatureView] which only hosts [derived views][derivedView] and doesn't render anything itself.
 *
 * [Binding][bind] this view is a no-op (unless [trackedUiState] is overridden).
 */
public interface HostFeatureView<in S : UiState> : FeatureView<S> {

	override val trackedUiState: List<(S) -> Any?>
		get() = SKIP_RENDER

	override fun render(uiState: S) {}
}

/**
 * For use as a value of [HostFeatureView.trackedUiState]. Denotes that rendering for the view should be skipped.
 */
internal val SKIP_RENDER: List<(UiState) -> Any?> = listOf { 0 }