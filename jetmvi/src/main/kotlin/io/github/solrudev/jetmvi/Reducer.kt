package io.github.solrudev.jetmvi

/**
 * Returns new UI state based on current UI state and an event affecting it.
 */
public interface Reducer<in E : Event, S : UiState> {
	public fun reduce(event: E, state: S): S
}