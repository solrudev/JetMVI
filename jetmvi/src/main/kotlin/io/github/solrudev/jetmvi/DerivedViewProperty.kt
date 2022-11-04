package io.github.solrudev.jetmvi

import kotlin.properties.ReadOnlyProperty

/**
 * Read-only property which stores derived [FeatureView].
 */
public interface DerivedViewProperty<in V, in S : UiState, out DV : FeatureView<S>> : ReadOnlyProperty<V, DV>, (V) -> DV