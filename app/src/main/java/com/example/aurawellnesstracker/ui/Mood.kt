package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.model.MoodEntry
import com.example.aurawellnesstracker.utils.MoodManager
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat

class Mood : AppCompatActivity() {

    private lateinit var moodManager: MoodManager
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var emptyHistoryText: TextView
    private lateinit var moodNotesEditText: EditText
    private lateinit var saveMoodButton: Button
    private lateinit var selectedMoodLayout: LinearLayout
    private lateinit var selectedEmoji: TextView
    private lateinit var selectedMoodText: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var moodHistoryRecyclerView: RecyclerView

    private var selectedMoodType: String? = null
    private var editingMoodEntry: MoodEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mood)

        moodManager = MoodManager(this)
        initializeViews()
        setupBottomNavigation()
        setupMoodSelection()
        setupRecyclerView()
        loadTodayMoodEntries()
        updateCurrentTime()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        emptyHistoryText = findViewById(R.id.emptyHistoryText)
        moodNotesEditText = findViewById(R.id.moodNotesEditText)
        saveMoodButton = findViewById(R.id.saveMoodButton)
        selectedMoodLayout = findViewById(R.id.selectedMoodLayout)
        selectedEmoji = findViewById(R.id.selectedEmoji)
        selectedMoodText = findViewById(R.id.selectedMoodText)
        currentTimeText = findViewById(R.id.currentTimeText)

        // Initialize RecyclerView - make sure this ID exists in your activity_mood.xml
        moodHistoryRecyclerView = findViewById(R.id.moodHistoryRecyclerView)

        saveMoodButton.setOnClickListener { saveMoodEntry() }

        // Set today's date
        val todayDateText = findViewById<TextView>(R.id.todayDateText)
        todayDateText.text = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    private fun setupMoodSelection() {
        // Primary moods from activity_mood.xml
        setupMoodCard(R.id.emojiTerrible, MoodEntry.MOOD_LOW, "😔")
        setupMoodCard(R.id.emojiSad, MoodEntry.MOOD_ANXIOUS, "😢")
        setupMoodCard(R.id.emojiNeutral, MoodEntry.MOOD_NEUTRAL, "😐")
        setupMoodCard(R.id.emojiHappy, MoodEntry.MOOD_GOOD, "😊")
        setupMoodCard(R.id.emojiExcited, MoodEntry.MOOD_EXCELLENT, "🤩")
    }

    private fun setupMoodCard(cardId: Int, moodType: String, emoji: String) {
        val cardView = findViewById<MaterialCardView?>(cardId)
        cardView?.setOnClickListener {
            selectMood(moodType, emoji)
        }
    }

    private fun selectMood(moodType: String, emoji: String) {
        selectedMoodType = moodType
        selectedEmoji.text = emoji
        selectedMoodText.text = moodType
        selectedMoodLayout.visibility = LinearLayout.VISIBLE

        // Update button text if editing
        if (editingMoodEntry != null) {
            saveMoodButton.text = "Update Mood Entry"
        } else {
            saveMoodButton.text = "Save Mood Entry"
        }
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            emptyList(),
            onEditClickListener = { moodEntry ->
                editMoodEntry(moodEntry)
            },
            onDeleteClickListener = { moodEntry ->
                showDeleteConfirmation(moodEntry)
            }
        )

        moodHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Mood)
            adapter = moodAdapter
        }
    }

    private fun loadTodayMoodEntries() {
        val todayEntries = moodManager.getTodayMoodEntries()
        moodAdapter.updateData(todayEntries)

        if (todayEntries.isEmpty()) {
            emptyHistoryText.visibility = TextView.VISIBLE
            moodHistoryRecyclerView.visibility = RecyclerView.GONE
        } else {
            emptyHistoryText.visibility = TextView.GONE
            moodHistoryRecyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun saveMoodEntry() {
        val moodType = selectedMoodType
        val notes = moodNotesEditText.text.toString().trim()

        if (moodType == null) {
            Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingMoodEntry != null) {
            val updatedEntry = editingMoodEntry!!.copy(
                moodType = moodType,
                emoji = MoodEntry.getEmojiForMood(moodType),
                notes = notes,
                date = Date(),
                timestamp = System.currentTimeMillis()
            )

            if (moodManager.updateMoodEntry(updatedEntry)) {
                Toast.makeText(this, "Mood updated successfully", Toast.LENGTH_SHORT).show()
                resetForm()
                loadTodayMoodEntries()
            } else {
                Toast.makeText(this, "Failed to update mood", Toast.LENGTH_SHORT).show()
            }
        } else {
            val newEntry = MoodEntry(
                moodType = moodType,
                emoji = MoodEntry.getEmojiForMood(moodType),
                notes = notes
            )

            moodManager.addMoodEntry(newEntry)
            Toast.makeText(this, "Mood saved successfully", Toast.LENGTH_SHORT).show()
            resetForm()
            loadTodayMoodEntries()
        }
    }

    private fun editMoodEntry(moodEntry: MoodEntry) {
        editingMoodEntry = moodEntry
        selectedMoodType = moodEntry.moodType
        selectedEmoji.text = moodEntry.emoji
        selectedMoodText.text = moodEntry.moodType
        moodNotesEditText.setText(moodEntry.notes)
        selectedMoodLayout.visibility = LinearLayout.VISIBLE
        saveMoodButton.text = "Update Mood Entry"

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView?.smoothScrollTo(0, 0)
    }

    private fun showDeleteConfirmation(moodEntry: MoodEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { dialog, which ->
                if (moodManager.deleteMoodEntry(moodEntry.id)) {
                    Toast.makeText(this, "Mood entry deleted", Toast.LENGTH_SHORT).show()
                    loadTodayMoodEntries()

                    if (editingMoodEntry?.id == moodEntry.id) {
                        resetForm()
                    }
                } else {
                    Toast.makeText(this, "Failed to delete mood entry", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetForm() {
        selectedMoodType = null
        editingMoodEntry = null
        moodNotesEditText.text.clear()
        selectedMoodLayout.visibility = LinearLayout.GONE
        saveMoodButton.text = "Save Mood Entry"
    }

    private fun updateCurrentTime() {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        currentTimeText.text = currentTime
    }

    private fun setupBottomNavigation() {
        val moodBtn = findViewById<ImageView>(R.id.productBtn10)

        moodBtn.setColorFilter(ContextCompat.getColor(this, R.color.primary_color))

        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        moodBtn.setOnClickListener {
            val intent = Intent(this, Mood::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.expertsBtn10).setOnClickListener {
            val intent = Intent(this, Habit::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.profileBtn10).setOnClickListener {
            val intent = Intent(this, Hydration::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}