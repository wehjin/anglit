package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.ScrollView
import rx.Observable
import rx.Observer
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

/**
 * @author wehjin
 * @since 2/23/14.
 */
class TreeView(context: Context, attrs: AttributeSet?, defStyle: Int) : ScrollView(context, attrs, defStyle) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val slidePanel = SlidePanel(context)
    private val scrollTopSubject = PublishSubject.create<Int>()
    private val clicksSubject = PublishSubject.create<Any>()
    private fun Adapter.createRows(depth: Int): List<RowModel> = tree.addRowsToList(depth, mutableListOf<RowModel>())
    val clicks: Observable<Any> get() = clicksSubject.asObservable().observeOn(Schedulers.trampoline())
    val scrollTops: Observable<Int> get() = scrollTopSubject.asObservable()

    var adapter: Adapter? by Delegates.observable(null as Adapter?) { property, oldAdapter, adapter ->

        val rows = adapter?.createRows(0) ?: emptyList()
        slidePanel.adapter = object : SlidePanel.Adapter {
            override val rows: List<RowModel> get() = rows
            override val clicksObserver: ((RowModel) -> Unit)? get() = { clicksSubject.onNext(it.treeTag) }

            override fun createView(): View {
                return adapter?.createView() ?: View(context)
            }

            override fun bindView(view: View, row: RowModel) {
                adapter?.bindView(view, row.treeTag)
            }
        }
    }

    init {
        addView(slidePanel)
        scrollTopSubject.distinctUntilChanged().subscribe(object : Observer<Int> {
            override fun onCompleted() {
                // Do nothing
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, e.message)
            }

            override fun onNext(scrollTop: Int) {
                slidePanel.visibleY = scrollTop
            }
        })
    }

    override fun scrollTo(x: Int, y: Int) {
        Log.d(TAG, "scrollTo $x,$y")
        super.scrollTo(x, y)
        slidePanel.visibleY = y
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        slidePanel.visibleHeight = height
    }

    override fun onScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
        super.onScrollChanged(left, top, oldLeft, oldTop)
        scrollTopSubject.onNext(top)
    }

    fun notifyRowsChanged(selector: (treeTag: Any) -> Boolean) {
        slidePanel.notifyRowsChanged(selector)
    }

    private fun Tree.addRowsToList(depth: Int, rows: MutableList<RowModel>): List<RowModel> {
        rows.add(RowModel(depth, tag))
        subTrees.forEach { it.addRowsToList(depth + 1, rows) }
        return rows
    }

    private class SlidePanel(context: Context, attrs: AttributeSet?, defStyle: Int) : ViewGroup(context, attrs, defStyle) {
        constructor(context: Context) : this(context, null, 0)
        constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

        interface Adapter {
            val rows: List<RowModel>
            val clicksObserver: ((RowModel) -> Unit)?
            fun createView(): View
            fun bindView(view: View, row: RowModel)
        }

        private val childViews: List<View> get() = (0 until childCount).map { this.getChildAt(it) }
        private val freeViews = ViewPool()
        private val boundViews = ViewPool()
        private val scratchViews = ViewPool()
        private var selectedView: View? by Delegates.observable(null as View?) { property, old, new ->
            if (new != old) {
                old?.isSelected = false
                new?.isSelected = true
            }
        }
        private var selectedRow: RowModel? = null
        val heightPixels: Int = context.resources.getDimensionPixelSize(R.dimen.cell_height)
        val indentPixels: Int = context.resources.getDimensionPixelSize(R.dimen.indent_width)

        var panelHeight: Int by Delegates.observable(0) { property, old, new ->
            if (new != old) {
                update(visibleY, visibleHeight, adapter, panelWidth, new)
                requestLayout()
            }
        }

        var panelWidth: Int? by Delegates.observable(null as Int?) { property, old, panelWidth ->
            if (panelWidth != old) {
                invalidateChildViews()
                update(visibleY, visibleHeight, adapter, panelWidth, panelHeight)
            }
        }

        var visibleY: Int by Delegates.observable(0) { property, old, visibleY ->
            if (visibleY != old) {
                update(visibleY, visibleHeight, adapter, panelWidth, panelHeight)
            }
        }

        var visibleHeight: Int by Delegates.observable(0) { property, old, new ->
            if (new != old) {
                update(visibleY, new, adapter, panelWidth, panelHeight)
            }
        }

        var adapter: Adapter? by Delegates.observable(null as Adapter?) { property, old, new ->
            if (new != old) {
                invalidateChildViews()
                panelHeight = (new?.rows?.size ?: 0) * heightPixels
                update(visibleY, visibleHeight, new, panelWidth, panelHeight)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), panelHeight)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            childViews.forEach {
                val row = it.tag as RowModel
                val viewWidth = Math.max(0, width - indentPixels * row.depth)
                it.measure(makeMeasureSpec(viewWidth, EXACTLY), makeMeasureSpec(heightPixels, EXACTLY))
                it.layout(width - viewWidth, 0, width, heightPixels)
            }
            post { panelWidth = width }
        }

        fun notifyRowsChanged(selector: (treeTag: Any) -> Boolean) {
            val adapter = adapter ?: return
            val affectedViews = boundViews.list().filter { selector((it.tag as RowModel).treeTag) }
            affectedViews.forEach {
                adapter.bindView(it, it.tag as RowModel)
            }
        }

        private fun update(visibleY: Int, visibleHeight: Int, adapter: Adapter?, panelWidth: Int?, panelHeight: Int) {
            Log.d(TAG, "Update visibleY: $visibleY visibleHeight: $visibleHeight panelWidth: $panelWidth panelHeight: $panelHeight adapter: $adapter")
            adapter ?: return
            panelWidth ?: return
            val rows = adapter.rows
            updateViewPositions(rows, visibleY, heightPixels)
            val visibleRows = rows.filter { it.viewPosition.isVisible(visibleY, visibleHeight) }
            visibleRows.reversed().forEach {
                val row = it
                val boundView = boundViews.pop(row.depth, { it.tag as RowModel == row })
                if (boundView != null) {
                    boundView.alpha = it.viewPosition.displayAlpha
                    boundView.bringToFront()
                    scratchViews.push(row.depth, boundView)
                } else {
                    val view = freeViews.pop(row.depth) ?: addView(adapter)
                    adapter.bindView(view, row)
                    view.tag = row
                    view.visibility = View.VISIBLE
                    view.setOnClickListener {
                        selectedView = view
                        selectedRow = row
                        adapter.clicksObserver?.invoke(row)
                    }
                    if (selectedRow == row) {
                        selectedView = view
                    } else {
                        view.isSelected = false
                    }
                    view.alpha = it.viewPosition.displayAlpha
                    view.bringToFront()
                    scratchViews.push(row.depth, view)
                }
            }
            transferBoundViewsToFree()
            transferScratchViewsToBound()
            boundViews.list().forEach {
                it.translationY = (it.tag as RowModel).viewPosition.top.toFloat()
            }
        }

        private fun addView(adapter: Adapter): View {
            val view = adapter.createView()
            addView(view)
            return view
        }

        private fun invalidateChildViews() {
            freeViews.clear()
            boundViews.clear()
            removeAllViews()
        }

        private fun transferBoundViewsToFree() {
            boundViews.transfer(freeViews, { depth, view -> view.visibility = View.INVISIBLE })
        }

        private fun transferScratchViewsToBound() {
            scratchViews.transfer(boundViews, { depth, view -> })
        }

        private val ViewPosition.displayAlpha: Float get() {
            val underAlpha = .1f
            if (offsetFromSticky > heightPixels) {
                return underAlpha
            } else if (offsetFromSticky > 0) {
                return underAlpha + (1f - underAlpha) * (1.0f - offsetFromSticky / heightPixels.toFloat())
            } else {
                return 1f
            }
        }

        private fun updateViewPositions(rows: List<RowModel>, visibleY: Int, heightPixels: Int) {
            val lowerRowTopAtDepth = mutableMapOf<Int, Int>()
            val lastIndex = rows.size - 1
            rows.reversed().forEachIndexed { i, row ->
                val index = lastIndex - i
                val normalBottom = (index + 1) * heightPixels
                val stickyBottom = visibleY + heightPixels * (row.depth + 1)
                val viewBottomBeforeLowerRowPressure = Math.max(normalBottom, stickyBottom)
                val viewBottomWithLowerRowPressure = getViewBottomWithLowerRowPressure(viewBottomBeforeLowerRowPressure, row.depth, lowerRowTopAtDepth)
                val viewTopAfterLowerRowPressure = viewBottomWithLowerRowPressure - heightPixels
                lowerRowTopAtDepth.put(row.depth, viewTopAfterLowerRowPressure)
                row.viewPosition.bottom = viewBottomWithLowerRowPressure
                row.viewPosition.top = viewTopAfterLowerRowPressure
                row.viewPosition.offsetFromSticky = stickyBottom - viewBottomWithLowerRowPressure
            }
        }

        private fun getViewBottomWithLowerRowPressure(viewBottomBeforeLowerRowPressure: Int, depth: Int, lowerRowTopsAtDepth: Map<Int, Int>): Int {
            val lowerRowTop = getHighestLowerRowTopForDepth(depth, lowerRowTopsAtDepth)
            return if (lowerRowTop == null)
                viewBottomBeforeLowerRowPressure
            else
                Math.min(viewBottomBeforeLowerRowPressure, lowerRowTop - 1)
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

        inner class ViewPool {
            private val views = mutableMapOf<Int, MutableList<View>>()

            fun pop(depth: Int, predicate: (View) -> Boolean): View? {
                val list = views[depth] ?: return null
                list.forEachIndexed { i, view ->
                    if (predicate(view)) {
                        list.removeAt(i)
                        return view
                    }
                }
                return null
            }

            fun pop(depth: Int): View? {
                val list = views[depth] ?: return null
                return if (list.isNotEmpty()) list.removeAt(0) else null
            }

            fun push(depth: Int, view: View) {
                views[depth]?.add(view) ?: views.put(depth, mutableListOf(view))
            }

            fun list(): List<View> = views.values.flatten()
            fun clear() = views.clear()

            fun transfer(destinationPool: ViewPool, each: (Int, View) -> Unit) {
                views.entries.forEach {
                    val depth = it.key
                    it.value.forEach {
                        each(depth, it)
                        destinationPool.push(depth, it)
                    }
                }
                this.clear()
            }
        }
    }

    private data class ViewPosition(var top: Int, var bottom: Int, var offsetFromSticky: Int) {
        fun isVisible(visibleY: Int, panelHeight: Int): Boolean = bottom >= visibleY && top < (visibleY + panelHeight)
    }

    private data class RowModel(var depth: Int, val treeTag: Any) {
        val viewPosition = ViewPosition(top = 0, bottom = 0, offsetFromSticky = 0)
    }

    interface Tree {
        val tag: Any
        val subTrees: List<Tree>
    }

    interface Adapter {
        val tree: Tree
        fun createView(): View
        fun bindView(view: View, treeTag: Any)
    }

    companion object {
        val TAG: String = TreeView::class.java.simpleName
    }
}
