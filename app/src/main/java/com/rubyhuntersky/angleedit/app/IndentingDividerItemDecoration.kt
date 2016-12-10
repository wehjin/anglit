package com.rubyhuntersky.angleedit.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class IndentingDividerItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {

    val dividerHeight = context.resources.getDimensionPixelSize(R.dimen.divider_height)
    val dividerIndent = context.resources.getDimensionPixelSize(R.dimen.divider_indent_small)
    val paint by lazy {
        val paint = Paint()
        paint.color = context.getColor(R.color.dividers_dark)
        paint
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) > 0) {
            outRect.top = dividerHeight
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft + dividerIndent
        val dividerRight = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + dividerHeight
            canvas.drawRect(dividerLeft.toFloat(), dividerTop.toFloat(), dividerRight.toFloat(), dividerBottom.toFloat(), paint)
        }
    }
}