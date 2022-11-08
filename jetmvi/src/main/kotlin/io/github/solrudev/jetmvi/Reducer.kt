package io.github.solrudev.jetmvi

/**
 * Returns new UI state based on current UI state and an event affecting it.
 */
public fun interface Reducer<in E : JetEvent, S : JetState> {
	public fun reduce(event: E, state: S): S
}