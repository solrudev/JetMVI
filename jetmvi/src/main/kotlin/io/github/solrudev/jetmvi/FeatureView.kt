package io.github.solrudev.jetmvi

/**
 * A view which can render UI state object.
 */
public interface FeatureView<in S : UiState> {
	public fun render(uiState: S) {}
}