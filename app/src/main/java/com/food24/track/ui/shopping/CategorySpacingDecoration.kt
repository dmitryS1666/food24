package com.food24.track.ui.shopping

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CategorySpacingDecoration(private val spacePx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos != RecyclerView.NO_POSITION) {
            val a = parent.adapter as? ShoppingSectionAdapter ?: return
            val row = a.getRowAt(pos)
            if (row is Row.Category) {
                outRect.top = spacePx      // или top & bottom, если хочешь
            }
        }
    }
}
