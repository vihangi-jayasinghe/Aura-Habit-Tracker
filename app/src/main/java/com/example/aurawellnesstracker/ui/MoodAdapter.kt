package com.example.aurawellnesstracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.model.MoodEntry
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onEditClickListener: (MoodEntry) -> Unit,
    private val onDeleteClickListener: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emojiText: TextView = itemView.findViewById(R.id.moodEmoji)
        private val moodTypeText: TextView = itemView.findViewById(R.id.moodType)
        private val timeText: TextView = itemView.findViewById(R.id.moodTime)
        private val notesText: TextView = itemView.findViewById(R.id.moodNotes)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.moodCard)
        private val editButton: ImageButton = itemView.findViewById(R.id.editMoodButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteMoodButton)

        fun bind(moodEntry: MoodEntry) {
            emojiText.text = moodEntry.emoji
            moodTypeText.text = moodEntry.moodType
            timeText.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(moodEntry.date)
            notesText.text = moodEntry.notes.ifEmpty { "No notes" }

            // Set all cards to light background color
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_background))

            editButton.setOnClickListener { onEditClickListener(moodEntry) }
            deleteButton.setOnClickListener { onDeleteClickListener(moodEntry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateData(newEntries: List<MoodEntry>) {
        moodEntries = newEntries.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}