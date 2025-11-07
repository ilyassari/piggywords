package com.ellez.piggywords.data.repository

import com.ellez.piggywords.data.local.WordCard
import com.ellez.piggywords.data.local.WordCardDao
import com.ellez.piggywords.data.local.WordReview
import com.ellez.piggywords.data.local.WordReviewDao
import com.ellez.piggywords.data.local.getCurrentDateString
import com.ellez.piggywords.data.local.getFutureDateString
import kotlinx.coroutines.flow.Flow
import kotlin.math.pow

class WordRepository(
    private val wordDao: WordCardDao,
    private val reviewDao: WordReviewDao
) {

    // All words
    fun getAllWords(): Flow<List<WordCard>> = wordDao.getAllWords()

    // Words by word type
    fun getWordsByType(wordType: String): Flow<List<WordCard>> =
        wordDao.getWordsByType(wordType)

    // Words to study (new logic)
    fun getWordsForStudy(limit: Int): Flow<List<WordCard>> {
        val today = getCurrentDateString()
        return wordDao.getWordsForStudy(today, limit)
    }

    // Pending word count (to be studied today)
    suspend fun getPendingWordCount(): Int {
        val today = getCurrentDateString()
        return wordDao.getPendingWordCount(today)
    }

    // Count of words studied today
    suspend fun getTodayStudiedCount(): Int {
        val today = getCurrentDateString()
        return reviewDao.getReviewCountForDate(today)
    }

    // Insert word
    suspend fun insertWord(word: WordCard) {
        wordDao.insertWord(word)
    }

    // Update word
    suspend fun updateWord(word: WordCard) {
        wordDao.updateWord(word)
    }

    // Delete word
    suspend fun deleteWord(word: WordCard) {
        wordDao.deleteWord(word)
        // Also delete associated reviews
        reviewDao.deleteReviewsForWord(word.id)
    }

    // Get word by ID
    suspend fun getWordById(id: Int): WordCard? {
        return wordDao.getWordById(id)
    }

    // Retire word
    suspend fun retireWord(id: Int) {
        wordDao.retireWord(id)
    }

    // Get word reviews
    fun getReviewsForWord(wordId: Int): Flow<List<WordReview>> {
        return reviewDao.getReviewsForWord(wordId)
    }

    suspend fun getReviewsForWordSync(wordId: Int): List<WordReview> {
        return reviewDao.getReviewsForWordSync(wordId)
    }

    // Review statistics
    suspend fun getReviewStats(wordId: Int): Pair<Int, Int> {
        val total = reviewDao.getReviewCount(wordId)
        val correct = reviewDao.getCorrectCount(wordId)
        return Pair(correct, total)
    }

    // NEW: Check if word already exists (for duplicate prevention)
    suspend fun isWordExists(word: String): Boolean {
        val allWords = wordDao.getAllWordsSync()
        return allWords.any {
            it.word.equals(word, ignoreCase = true)
        }
    }

    // NEW: Get all existing words (for filtering pool)
    suspend fun getAllExistingWords(): List<String> {
        val allWords = wordDao.getAllWordsSync()
        return allWords.map { it.word }
    }

    // Update word after review (NEW LOGIC)
    suspend fun updateWordAfterReview(wordId: Int, isCorrect: Boolean) {
        val word = wordDao.getWordById(wordId) ?: return
        val today = getCurrentDateString()

        // Calculate new level
        val newLevel = if (isCorrect) {
            word.learningLevel + 1
        } else {
            maxOf(0, word.learningLevel - 1)
        }

        // Calculate next presentation date
        val daysToAdd = word.frequencyMultiplier.toDouble()
            .pow(newLevel.toDouble())
            .toLong()
        val nextDate = getFutureDateString(daysToAdd)

        // Update WordCard
        val updatedWord = word.copy(
            learningLevel = newLevel,
            nextReviewDate = nextDate,
            scheduledDate = nextDate,
            lastReviewedDate = today,
            totalReviews = word.totalReviews + 1
        )
        wordDao.updateWord(updatedWord)

        // Add review record
        val review = WordReview(
            wordId = wordId,
            reviewDate = today,
            isCorrect = isCorrect,
            learningLevel = newLevel
        )
        reviewDao.insertReview(review)
    }
}