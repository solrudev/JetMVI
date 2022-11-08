package io.github.solrudev.jetmvi

/**
 * Transforms events of type [InEvent] to events of type [OutEvent].
 *
 * If event should be ignored, return null.
 */
public fun interface JetEventMapper<in InEvent : JetEvent, out OutEvent : JetEvent?> : (InEvent) -> OutEvent {
	override fun invoke(event: InEvent): OutEvent
}

/**
 * Transforms UI state of type [InState] to UI state of type [OutState].
 */
public fun interface JetStateMapper<in InState : JetState, out OutState : JetState> : (InState) -> OutState {
	override fun invoke(uiState: InState): OutState
}