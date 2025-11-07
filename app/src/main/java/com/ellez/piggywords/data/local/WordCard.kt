package com.ellez.piggywords.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper function to get the current date as a String in 'yyyy-MM-dd' format.
 * This is used instead of java.time.LocalDate.now() to support API levels below 26.
 */
fun getCurrentDateString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

/**
 * Helper function to get a future date as a String in 'yyyy-MM-dd' format.
 * Adds the specified number of days to the current date.
 */
fun getFutureDateString(daysToAdd: Long): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd.toInt())
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(calendar.time)
}

@Entity(tableName = "word_cards")
data class WordCard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val word: String,

    /**
     * Word type stored as comma-separated type IDs
     * Format: "1,11" means Noun + Idiom
     * Single type: "2" means Verb
     * Use WordTypes utility to convert between IDs and display names
     */
    val wordType: String,

    val definition: String,
    val translation: String,
    val imagePath: String? = null,

    val learningLevel: Int = 0,
    val nextReviewDate: String,
    val frequencyMultiplier: Int = 2,

    val scheduledDate: String = getCurrentDateString(),
    val lastReviewedDate: String? = null,
    val totalReviews: Int = 0,

    val createdAt: String = getCurrentDateString(),
    val isRetired: Boolean = false
)