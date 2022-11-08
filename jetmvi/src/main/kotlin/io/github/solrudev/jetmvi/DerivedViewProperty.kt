package io.github.solrudev.jetmvi

import kotlin.properties.ReadOnlyProperty

/**
 * Read-only property which stores derived [JetView].
 */
public interface DerivedViewProperty<in V, in S : JetState, out DV : JetView<S>> : ReadOnlyProperty<V, DV>, (V) -> DV