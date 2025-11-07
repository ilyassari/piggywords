package com.ellez.piggywords.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_reviews")
data class WordReview(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val wordId: Int,
    val reviewDate: String,
    val isCorrect: Boolean,
    val learningLevel: Int,
    val createdAt: String = getCurrentDateString()
)