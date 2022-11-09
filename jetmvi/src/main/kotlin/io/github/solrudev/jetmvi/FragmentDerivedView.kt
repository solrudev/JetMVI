package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlin.reflect.KProperty

/**
 * Returns a property delegate for accessing [JetView] which is derived from the current fragment (i.e. sharing
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
			  V : Fragment {
	return FragmentDerivedViewProperty(this, derivedViewProducer)
}

private class FragmentDerivedViewProperty<in V, in S : JetState, out DV : JetView<S>>(
	private var fragment: V?,
	private val derivedViewProducer: V.() -> DV
) : DerivedViewProperty<V, S, DV>, DefaultLifecycleObserver
		where V : JetView<S>,
			  V : Fragment {

	private var derivedView: DV? = null
	private var hasView = false
	private var isViewDestroyed = false

	private val fragmentViewCallback = Observer<LifecycleOwner?> { viewLifecycleOwner ->
		if (viewLifecycleOwner != null) {
			hasView = true
			isViewDestroyed = false
		} else if (hasView) {
			derivedView = null
			isViewDestroyed = true
		}
	}

	init {
		fragment?.let { f ->
			f.lifecycle.addObserver(this)
			f.viewLifecycleOwnerLiveData.observeForever(fragmentViewCallback)
		}
	}

	override fun onDestroy(owner: LifecycleOwner) {
		owner.lifecycle.removeObserver(this)
		onDestroy()
	}

	override fun getValue(thisRef: V, property: KProperty<*>): DV {
		return invoke(thisRef)
	}

	override fun invoke(thisRef: V): DV {
		checkFragmentLifecycle(thisRef)
		return derivedView ?: thisRef.derivedViewProducer().also { derivedView = it }
	}

	private fun onDestroy() {
		fragment?.viewLifecycleOwnerLiveData?.removeObserver(fragmentViewCallback)
		derivedView = null
		fragment = null
		hasView = false
		isViewDestroyed = false
	}

	private fun checkFragmentLifecycle(thisRef: V) {
		if (thisRef.lifecycle.currentState == Lifecycle.State.DESTROYED) {
			error("Accessing derived view in fragment when fragment is destroyed.")
		}
		if (isViewDestroyed) {
			error("Accessing derived view in fragment after onDestroyView().")
		}
	}
}