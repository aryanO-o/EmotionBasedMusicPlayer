package com.example.emotionbasedmusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.emojiDataModel
import com.example.emotionbasedmusic.helper.Constants

class emojiAdapter (
    private val listener: Ilistener,
    private val dataset: List<emojiDataModel>
) : RecyclerView.Adapter<emojiAdapter.emojiViewHolder>() {

    interface Ilistener {
        fun onItemClick(mood: String)
    }
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
        holder.itemView.setOnClickListener {
            if(item.emojiText == Constants.SURPRISED_MOOD || item.emojiText == Constants.TIRED_MOOD) {
                listener.onItemClick(Constants.SAD_MOOD)
            }
            else {
                listener.onItemClick(item.emojiText)
            }

        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}