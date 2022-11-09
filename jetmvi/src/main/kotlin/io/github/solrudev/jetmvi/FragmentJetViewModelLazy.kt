package io.github.solrudev.jetmvi

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * Returns a property delegate to access [JetViewModel] scoped to this [Fragment] and [binds][bind] it.
 *
 * If you have [derived views][derivedView] in your fragment which should be bound to the fragment's [JetViewModel],
 * you can [bind][bindDerived] them by passing them to this delegate function.
 *
 * Example:
 * ```
 * val derivedView1 by derivedView { DerivedView1(viewBinding, viewModel) }
 * val derivedView2 by derivedView { DerivedView2(viewBinding, viewModel) }
 * val viewModel: MyJetViewModel by jetViewModels(MyFragment::derivedView1, MyFragment::derivedView2)
 * ```
 * or
 * ```
 * val viewModel: MyJetViewModel by jetViewModels(
 *     { myFragment -> myFragment.derivedView1 },
 *     { myFragment -> myFragment.derivedView2 }
 * )
 * ```
 *
 * @param derivedViewProducer function which returns view derived from this fragment. Derived view will be bound to the
 * created JetViewModel. Derived views are created with [derivedView] delegate.
 */
public inline fun <reified VM, S : JetState, V> V.jetViewModels(
	vararg derivedViewProducer: V.() -> JetView<S>,
	noinline ownerProducer: () -> ViewModelStoreOwner = { this },
	noinline extrasProducer: (() -> CreationExtras)? = null,
	noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM>
		where V : JetView<S>,
			  V : Fragment,
			  VM : ViewModel,
			  VM : JetViewModel<S> {
	val viewModelLazy = viewModels<VM>(ownerProducer, extrasProducer, factoryProducer)
	return FragmentJetViewModelLazy(this, viewModelLazy, derivedViewProducer)
}

@PublishedApi
internal class FragmentJetViewModelLazy<out VM, S : JetState, in V>(
	private var fragment: V?,
	private val viewModelLazy: Lazy<VM>,
	private var derivedViewProducers: Array<out (V.() -> JetView<S>)>
) : Lazy<VM> by viewModelLazy, DefaultLifecycleObserver
		where V : JetView<S>,
			  V : Fragment,
			  VM : ViewModel,
			  VM : JetViewModel<S> {

	private val V.derivedViews: Array<out JetView<S>>
		get() = Array(derivedViewProducers.size) { index -> derivedViewProducers[index].invoke(this) }

	private val viewModel by viewModelLazy
	private var isBound = false

	private val bindViewModelCallback = Observer<LifecycleOwner?> { viewLifecycleOwner ->
		if (viewLifecycleOwner != null) {
			isBound = true
			val f = fragment ?: return@Observer
			viewModel.bind(f, f.derivedViews, viewLifecycleOwner.lifecycleScope, viewLifecycleOwner.lifecycle)
		}
	}

	init {
		fragment?.let { f ->
			f.lifecycle.addObserver(this)
			f.viewLifecycleOwnerLiveData.observeForever(bindViewModelCallback)
		}
	}

	override fun onStart(owner: LifecycleOwner) {
		if (!isBound) { // may be only if fragment doesn't have a view
			fragment?.let(::bindHeadless)
		}
	}

	override fun onDestroy(owner: LifecycleOwner) {
		owner.lifecycle.removeObserver(this)
		onDestroy()
	}

	private fun onDestroy() {
		fragment?.viewLifecycleOwnerLiveData?.removeObserver(bindViewModelCallback)
		fragment = null
		isBound = false
	}

	private fun bindHeadless(fragment: V) {
		viewModel.bind(fragment, fragment.derivedViews, fragment.lifecycleScope, fragment.lifecycle)
		isBound = true
	}
}