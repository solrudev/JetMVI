package io.github.solrudev.jetmvi

/**
 * Transforms events of type [InEvent] to events of type [OutEvent].
 */
public interface EventMapper<in InEvent : Event, out OutEvent : Event> : (InEvent) -> OutEvent {
	override fun invoke(event: InEvent): OutEvent
}

/**
 * Transforms UI state of type [InUiState] to UI state of type [OutUiState].
 */
public interface UiStateMapper<in InUiState : UiState, out OutUiState : UiState> : (InUiState) -> OutUiState {
	override fun invoke(uiState: InUiState): OutUiState
}