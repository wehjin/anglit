package com.rubyhuntersky.angleedit.app

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * @author wehjin
 * *
 * @since 2/23/14.
 */

/**
 * View displays a tree.
 */
class TreeView(context: Context, attrs: AttributeSet?, defStyle: Int) : ScrollView(context, attrs, defStyle) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    interface TreeModel {
        fun newViewInstance(): View

        val tag: Any

        val subTrees: List<TreeModel>

        object Empty : TreeModel {
            override fun newViewInstance(): View {
                throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override val tag: Any get() = throw UnsupportedOperationException()
            override val subTrees: List<TreeModel> get() = throw UnsupportedOperationException()
        }
    }

    internal class RowModel(var depth: Int, val view: View, val tag: Any)

    private var slidePanel: SlidePanel
    private val rowModels = ArrayList<RowModel>()
    private val scrollTop = PublishSubject.create<Int>()
    private val selectionSubject = BehaviorSubject.create<Any>()

    init {
        slidePanel = SlidePanel(context)
        addView(slidePanel)
        scrollTop.throttleWithTimeout(5, TimeUnit.MILLISECONDS).distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Int> {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                        Log.d("TreeView", e.message)
                    }

                    override fun onNext(args: Int?) {
                        slidePanel.requestLayout()
                    }
                })
    }

    val selections: Observable<Any>
        get() = selectionSubject.asObservable().distinctUntilChanged().observeOn(Schedulers.trampoline())

    var tree: TreeModel by Delegates.observable(TreeModel.Empty as TreeModel) { property, oldValue, newValue ->
        rowModels.clear()
        rowModels.addAll(newValue.createRows(0))
        slidePanel.setupViews(rowModels, selectionSubject)
        requestLayout()
    }

    override fun onScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
        super.onScrollChanged(left, top, oldLeft, oldTop)
        scrollTop.onNext(top)
    }

    private fun TreeModel.createRows(depth: Int): List<RowModel> {
        val rows = mutableListOf(RowModel(depth, newViewInstance(), tag))
        rows.addAll(subTrees.map { subTree -> subTree.createRows(depth + 1) }.flatten())
        return rows
    }

    internal inner class SlidePanel : ViewGroup {

        val views: MutableList<View> = ArrayList()
        private var heightPixels: Int = 0
        private var indentPixels: Int = 0
        private var selectedView: View? = null

        @SuppressLint("UseSparseArrays")
        private val lowerRowTopAtDepth = HashMap<Int, Int>()

        constructor(context: Context) : super(context) {
            initSlidePanel(context)
        }

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            initSlidePanel(context)
        }

        constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
            initSlidePanel(context)
        }

        private fun initSlidePanel(context: Context) {
            heightPixels = context.resources.getDimensionPixelSize(R.dimen.cell_height)
            indentPixels = context.resources.getDimensionPixelSize(R.dimen.indent_width)
        }

        fun setupViews(flatModels: List<RowModel>, selectionObserver: Observer<Any>) {
            removeAllViews()
            views.clear()
            for (flatModel in flatModels) {
                val view = flatModel.view
                view.tag = flatModel
                addView(view, 0)
                views.add(view)

                view.setOnClickListener { view ->
                    Log.d("TreeView", "Clicked")
                    if (selectedView != null) {
                        selectedView!!.isSelected = false
                    }
                    view.isSelected = true
                    selectedView = view
                    selectionObserver.onNext(flatModel.tag)
                }
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = View.MeasureSpec.getSize(widthMeasureSpec)
            setMeasuredDimension(width, views.size * heightPixels)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val width = width
            val scrollY = this@TreeView.scrollY
            lowerRowTopAtDepth.clear()
            for (index in views.indices.reversed()) {
                val view = views[index]
                val rowModel = view.tag as RowModel

                val viewWidth = width - indentPixels * rowModel.depth

                val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY)
                val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightPixels,
                        View.MeasureSpec.EXACTLY)
                view.measure(widthMeasureSpec, heightMeasureSpec)
                view.layout(width - viewWidth, 0, width, heightPixels)

                val viewNormalBottom = (index + 1) * heightPixels

                val stickyYBottom = scrollY + heightPixels * (rowModel.depth + 1)
                val viewBottomBeforeLowerRowPressure = Math.max(viewNormalBottom, stickyYBottom)
                val viewBottomWithLowerRowPressure = getViewBottomWithLowerRowPressure(
                        viewBottomBeforeLowerRowPressure, rowModel.depth)
                val offsetFromStickyY = stickyYBottom - viewBottomWithLowerRowPressure
                view.alpha = getRowAlpha(offsetFromStickyY)

                val viewTopAfterLowerRowPressure = viewBottomWithLowerRowPressure - heightPixels
                view.translationY = viewTopAfterLowerRowPressure.toFloat()
                lowerRowTopAtDepth.put(rowModel.depth, viewTopAfterLowerRowPressure)
            }
        }

        private fun getViewBottomWithLowerRowPressure(viewBottomBeforeLowerRowPressure: Int,
                                                      depth: Int): Int {
            val lowerRowTop = getHighestLowerRowTopForDepth(depth)
            return if (lowerRowTop == null)
                viewBottomBeforeLowerRowPressure
            else
                Math.min(
                        viewBottomBeforeLowerRowPressure, lowerRowTop - 1)
        }

        private fun getRowAlpha(offsetFromStickyY: Int): Float {
            val underAlpha = .1f
            if (offsetFromStickyY > heightPixels) {
                return underAlpha
            } else if (offsetFromStickyY > 0) {
                return underAlpha + (1f - underAlpha) * (1.0f - offsetFromStickyY / heightPixels.toFloat())
            } else {
                return 1f
            }
        }

        private fun getHighestLowerRowTopForDepth(depth: Int): Int? {
            var lowerRowTop: Int? = null
            for (i in 0..depth) {
                val lowerRowTopAtDepth = this.lowerRowTopAtDepth[i]
                if (lowerRowTopAtDepth != null) {
                    if (lowerRowTop == null) {
                        lowerRowTop = lowerRowTopAtDepth
                    } else {
                        lowerRowTop = Math.min(lowerRowTop, lowerRowTopAtDepth)
                    }
                }
            }
            return lowerRowTop
        }

    }
}
