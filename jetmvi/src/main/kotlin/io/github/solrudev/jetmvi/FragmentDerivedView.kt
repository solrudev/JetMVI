package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlin.reflect.KProperty

/**
 * Returns a property delegate for accessing [JetView] which is derived from the current fragment (i.e. sharing
 * its [JetState] and [JetViewModel]).
 *
 * Derived view reference is released **before** `onDestroy()`, and accessing it after that will throw
 * [IllegalStateException].
 *
 * **For fragment with a view:**
 *
 * In fragment with a view, derived view reference will be released when fragment's view is destroyed, and derived view
 * will be recreated on first access after that (if using [jetViewModels] or [activityJetViewModels] delegate, it
 * happens automatically when fragment's view is recreated). When accessing derived view after `onDestroyView()` and
 * before `onCreateView()` returns, this delegate will throw [IllegalStateException].
 *
 * **Example of usage:**
 * ```
 * class SomeView(
 *     val viewBinding: MyLayoutBinding,
 *     val viewModel: MyJetViewModel
 * ) : JetView<MyUiState> { ... }
 *
 * class MyFragment : Fragment(), JetView<MyUiState> {
 *     val someView by derivedView { SomeView(viewBinding!!, viewModel) }
 *     var viewBinding: MyLayoutBinding? = null
 *     val viewModel: MyJetViewModel by jetViewModels(MyFragment::someView)
 *     ...
 * }
 * ```
 *
 * @param derivedViewProducer function returning derived view. It has parent view as its receiver.
 */
public fun <DV : JetView<S>, S : JetState, V> V.derivedView(
	derivedViewProducer: V.() -> DV
): DerivedViewProperty<V, S, DV>
		where V : JetView<S>,
			  V : Fragment {
	return FragmentDerivedViewProperty(this, derivedViewProducer, viewLifecycleOwnerLiveData)
}

private class FragmentDerivedViewProperty<in V : LifecycleOwner, in S : JetState, out DV : JetView<S>>(
	fragment: LifecycleOwner,
	private val derivedViewProducer: V.() -> DV,
	private var viewLifecycleOwnerLiveData: LiveData<LifecycleOwner?>?
) : DerivedViewProperty<V, S, DV>, DefaultLifecycleObserver {

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
		fragment.lifecycle.addObserver(this)
		viewLifecycleOwnerLiveData?.observeForever(fragmentViewCallback)
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
		derivedView = null
		viewLifecycleOwnerLiveData?.removeObserver(fragmentViewCallback)
		viewLifecycleOwnerLiveData = null
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