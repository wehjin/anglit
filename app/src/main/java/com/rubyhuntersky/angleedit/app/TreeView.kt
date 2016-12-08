package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import rx.Observable
import rx.Observer
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.*
import kotlin.properties.Delegates

/**
 * @author wehjin
 * @since 2/23/14.
 */
class TreeView(context: Context, attrs: AttributeSet?, defStyle: Int) : ScrollView(context, attrs, defStyle) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    interface Adapter {
        fun newViewInstance(): View

        val tag: Any

        val subAdapters: List<Adapter>

        object Empty : Adapter {
            override fun newViewInstance(): View {
                throw UnsupportedOperationException("not implemented") //To change bodyView of created functions use File | Settings | File Templates.
            }

            override val tag: Any get() = throw UnsupportedOperationException()
            override val subAdapters: List<Adapter> get() = throw UnsupportedOperationException()
        }
    }

    private class RowModel(var depth: Int, val view: View, val tag: Any) {
        val viewPosition = ViewPosition(0, 0, 0)

        init {
            view.tag = this
        }
    }

    private var slidePanel: SlidePanel
    private val rowModels = ArrayList<RowModel>()
    private val scrollTopSubject = PublishSubject.create<Int>()
    private val selectionSubject = BehaviorSubject.create<Any>()

    init {
        slidePanel = SlidePanel(context)
        addView(slidePanel)
        scrollTopSubject.distinctUntilChanged().subscribe(object : Observer<Int> {
            override fun onCompleted() {
                // Do nothing
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, e.message)
            }

            override fun onNext(scrollTop: Int) = slidePanel.moveViews(scrollTop)
        })
    }

    val selections: Observable<Any> get() = selectionSubject.asObservable().distinctUntilChanged().observeOn(Schedulers.trampoline())
    val scrollTop: Observable<Int> get() = scrollTopSubject.asObservable()

    var adapter: Adapter by Delegates.observable(Adapter.Empty as Adapter) { property, oldValue, newValue ->
        rowModels.clear()
        rowModels.addAll(newValue.createRows(0))
        slidePanel.setupViews(rowModels, selectionSubject)
        requestLayout()
    }

    override fun onScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
        super.onScrollChanged(left, top, oldLeft, oldTop)
        scrollTopSubject.onNext(top)
    }

    private fun Adapter.createRows(depth: Int): List<RowModel> {
        val rows = mutableListOf(RowModel(depth, newViewInstance(), tag))
        rows.addAll(subAdapters.map { subTree -> subTree.createRows(depth + 1) }.flatten())
        return rows
    }

    private inner class SlidePanel(context: Context, attrs: AttributeSet?, defStyle: Int) : ViewGroup(context, attrs, defStyle) {
        constructor(context: Context) : this(context, null, 0)
        constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

        val views: MutableList<View> = ArrayList()
        private var heightPixels: Int = 0
        private var indentPixels: Int = 0
        private var selectedView: View? = null

        init {
            heightPixels = context.resources.getDimensionPixelSize(R.dimen.cell_height)
            indentPixels = context.resources.getDimensionPixelSize(R.dimen.indent_width)
        }

        fun setupViews(rows: List<RowModel>, selectionObserver: Observer<Any>) {
            removeAllViews()
            views.clear()
            for (row in rows) {
                val view = row.view
                view.setOnClickListener { view ->
                    if (selectedView != null) {
                        selectedView!!.isSelected = false
                    }
                    view.isSelected = true
                    selectedView = view
                    selectionObserver.onNext(row.tag)
                }
                addView(view, 0)
                views.add(view)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), views.size * heightPixels)

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val width = width
            views.forEachIndexed { i, view ->
                val row = view.tag as RowModel
                val viewWidth = width - indentPixels * row.depth
                val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY)
                val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightPixels, View.MeasureSpec.EXACTLY)
                view.measure(widthMeasureSpec, heightMeasureSpec)
                view.layout(width - viewWidth, 0, width, heightPixels)
            }
            moveViews(this@TreeView.scrollY)
        }

        fun moveViews(scrollY: Int) {
            computeViewPositions(scrollY)
            translateViews()
        }

        private fun computeViewPositions(scrollY: Int) {
            val lowerRowTopAtDepth = mutableMapOf<Int, Int>()
            for (index in views.indices.reversed()) {
                val view = views[index]
                val row = view.tag as RowModel
                val normalBottom = (index + 1) * heightPixels
                val stickyBottom = scrollY + heightPixels * (row.depth + 1)
                val viewBottomBeforeLowerRowPressure = Math.max(normalBottom, stickyBottom)
                val viewBottomWithLowerRowPressure = getViewBottomWithLowerRowPressure(viewBottomBeforeLowerRowPressure, row.depth, lowerRowTopAtDepth)
                val offsetFromStickyY = stickyBottom - viewBottomWithLowerRowPressure
                val viewTopAfterLowerRowPressure = viewBottomWithLowerRowPressure - heightPixels
                lowerRowTopAtDepth.put(row.depth, viewTopAfterLowerRowPressure)
                row.viewPosition.bottom = viewBottomWithLowerRowPressure
                row.viewPosition.top = viewTopAfterLowerRowPressure
                row.viewPosition.offsetFromSticky = offsetFromStickyY
            }
        }

        private fun translateViews() {
            views.forEach { view ->
                val row = view.tag as RowModel
                val viewPosition = row.viewPosition
                view.alpha = getRowAlpha(viewPosition.offsetFromSticky)
                view.translationY = viewPosition.top.toFloat()
            }
        }

        private fun getViewBottomWithLowerRowPressure(viewBottomBeforeLowerRowPressure: Int, depth: Int, lowerRowTopsAtDepth: Map<Int, Int>): Int {
            val lowerRowTop = getHighestLowerRowTopForDepth(depth, lowerRowTopsAtDepth)
            return if (lowerRowTop == null)
                viewBottomBeforeLowerRowPressure
            else
                Math.min(viewBottomBeforeLowerRowPressure, lowerRowTop - 1)
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

        private fun getHighestLowerRowTopForDepth(depth: Int, lowerRowTopsAtDepth: Map<Int, Int>): Int? {
            var lowerRowTop: Int? = null
            for (i in 0..depth) {
                val lowerRowTopAtDepth = lowerRowTopsAtDepth[i]
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

    data private class ViewPosition(var top: Int, var bottom: Int, var offsetFromSticky: Int)

    companion object {
        val TAG: String = TreeView::class.java.simpleName
    }
}
