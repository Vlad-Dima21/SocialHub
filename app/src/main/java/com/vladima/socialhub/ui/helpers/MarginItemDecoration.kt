package com.vladima.socialhub.ui.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * https://cesarmorigaki.medium.com/a-better-way-to-set-recyclerview-items-margin-708ea9d3ac25
 */
class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceSize
            }
            bottom = spaceSize
        }
    }
}