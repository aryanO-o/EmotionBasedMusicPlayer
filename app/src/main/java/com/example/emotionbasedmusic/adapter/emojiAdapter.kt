package com.example.emotionbasedmusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.model.emojiDataModel

class emojiAdapter (
    private val context: Context,
    private val dataset: List<emojiDataModel>
) : RecyclerView.Adapter<emojiAdapter.emojiViewHolder>() {


    class emojiViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val emojiImageView: ImageView = view.findViewById(R.id.emoji_image)
        val emojiTextView: TextView = view.findViewById(R.id.emoji_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): emojiViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.emoji_list_item, parent, false)
        return emojiViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: emojiViewHolder, position: Int) {
        val item = dataset[position]
        holder.emojiImageView.setImageResource(item.emojiResourceId)
        holder.emojiTextView.text = item.emojiText
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}