package com.example.emotionbasedmusic.Utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager as RecyclerView.LayoutManager?) ?: RecyclerView.NO_POSITION
    return snapView.let { layoutManager.getPosition(it as View) }
}