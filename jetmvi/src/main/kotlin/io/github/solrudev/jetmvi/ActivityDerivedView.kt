package io.github.solrudev.jetmvi

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KProperty

/**
 * Returns a property delegate for accessing [JetView] which is derived from the current activity (i.e. sharing
 * its [JetState] and [JetViewModel]).
 *
 * Example:
 * ```
 * class SomeView(
 *     val viewBinding: MyLayoutBinding,
 *     val viewModel: MyJetViewModel
 * ) : JetView<MyUiState> { ... }
 * ...
 * val someView by derivedView { SomeView(viewBinding, viewModel) }
 * ```
 *
 * @param derivedViewProducer function returning derived view. It has parent view as its receiver.
 */
public fun <DV : JetView<S>, S : JetState, V> V.derivedView(
	derivedViewProducer: V.() -> DV
): DerivedViewProperty<V, S, DV>
		where V : JetView<S>,
			  V : ComponentActivity {
	return ActivityDerivedViewProperty(this, derivedViewProducer)
}

private class ActivityDerivedViewProperty<in V, in S : JetState, out DV : JetView<S>>(
	activity: V,
	private val derivedViewProducer: V.() -> DV
) : DerivedViewProperty<V, S, DV>, DefaultLifecycleObserver
		where V : JetView<S>,
			  V : ComponentActivity {

	private var derivedView: DV? = null

	init {
		activity.lifecycle.addObserver(this)
	}

	override fun onDestroy(owner: LifecycleOwner) {
		owner.lifecycle.removeObserver(this)
		onDestroy()
	}

	override fun getValue(thisRef: V, property: KProperty<*>): DV {
		return invoke(thisRef)
	}

	override fun invoke(thisRef: V): DV {
		checkActivityLifecycle(thisRef)
		return derivedView ?: thisRef.derivedViewProducer().also { derivedView = it }
	}

	private fun onDestroy() {
		derivedView = null
	}

	private fun checkActivityLifecycle(thisRef: V) {
		if (thisRef.lifecycle.currentState == Lifecycle.State.DESTROYED) {
			error("Accessing derived view in activity when activity is destroyed.")
		}
	}
}