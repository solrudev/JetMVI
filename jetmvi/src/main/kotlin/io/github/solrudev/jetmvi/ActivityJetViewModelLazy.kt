package io.github.solrudev.jetmvi

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * Returns a property delegate to access [JetViewModel] scoped to this [ComponentActivity] and [binds][bind] it.
 *
 * It uses Jetpack [viewModels] delegate under the hood.
 *
 * If you have [derived views][derivedView] in your activity which should be bound to the activity's [JetViewModel],
 * you can [bind][bindDerived] them by passing them to this delegate function.
 *
 * Example:
 * ```
 * val derivedView1 by derivedView { DerivedView1(viewBinding, viewModel) }
 * val derivedView2 by derivedView { DerivedView2(viewBinding, viewModel) }
 * val viewModel: MyJetViewModel by jetViewModels(MyActivity::derivedView1, MyActivity::derivedView2)
 * ```
 * or
 * ```
 * val viewModel: MyJetViewModel by jetViewModels(
 *     { myActivity -> myActivity.derivedView1 },
 *     { myActivity -> myActivity.derivedView2 }
 * )
 * ```
 *
 * @param derivedViewProducer function which returns view derived from this activity. Derived view will be bound to the
 * created JetViewModel. Derived views are created with [derivedView] delegate.
 * @see [viewModels]
 */
public inline fun <reified VM, S : JetState, V> V.jetViewModels(
	vararg derivedViewProducer: V.() -> JetView<S>,
	noinline extrasProducer: (() -> CreationExtras)? = null,
	noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM>
		where V : JetView<S>,
			  V : ComponentActivity,
			  VM : ViewModel,
			  VM : JetViewModel<S> {
	val viewModelLazy = viewModels<VM>(extrasProducer, factoryProducer)
	return ActivityJetViewModelLazy(this, viewModelLazy, derivedViewProducer)
}

@PublishedApi
internal class ActivityJetViewModelLazy<out VM, S : JetState, in V>(
	private var activity: V?,
	private val viewModelLazy: Lazy<VM>,
	private var derivedViewProducers: Array<out (V.() -> JetView<S>)>
) : Lazy<VM> by viewModelLazy, DefaultLifecycleObserver
		where V : JetView<S>,
			  V : ComponentActivity,
			  VM : ViewModel,
			  VM : JetViewModel<S> {

	private val V.derivedViews: Array<out JetView<S>>
		get() = Array(derivedViewProducers.size) { index -> derivedViewProducers[index].invoke(this) }

	private val viewModel by viewModelLazy

	init {
		activity?.lifecycle?.addObserver(this)
	}

	override fun onCreate(owner: LifecycleOwner) {
		val activity = this.activity ?: return
		viewModel.bind(activity, activity.derivedViews, activity.lifecycleScope, activity.lifecycle)
	}

	override fun onDestroy(owner: LifecycleOwner) {
		owner.lifecycle.removeObserver(this)
		onDestroy()
	}

	private fun onDestroy() {
		activity = null
		derivedViewProducers = emptyArray()
	}
}