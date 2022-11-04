package io.github.solrudev.jetmvi

/**
 * Marker interface for UI state objects.
 */
public interface UiState

/**
 * Marker interface for events.
 */
public interface Event

/**
 * Marker interface for side effects.
 */
public interface Effect

/**
 * Marker interface for events which should be ignored when transformed to another type.
 */
public interface MappedEventToIgnore : Event