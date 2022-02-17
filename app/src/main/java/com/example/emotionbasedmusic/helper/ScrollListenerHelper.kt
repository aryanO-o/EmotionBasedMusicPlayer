package com.example.emotionbasedmusic.helper

import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.Utils.getSnapPosition

class ScrollListenerHelper(private val listener: OnSnapPositionChangeListener, private val pagerSnapHelper: PagerSnapHelper, private var snapPosition: Int):
    RecyclerView.OnScrollListener() {

    interface OnSnapPositionChangeListener {
        fun onSnapPositionChange(position: Int)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            notifySnapPositionChanged(recyclerView)
        }
    }

    private fun notifySnapPositionChanged(recyclerView: RecyclerView) {
        val snapPosition = pagerSnapHelper.getSnapPosition(recyclerView)
        val snapPositionChanged = this.snapPosition != snapPosition
        if (snapPositionChanged) {
            listener.onSnapPositionChange(snapPosition)
            this.snapPosition = snapPosition
        }
    }

}