package com.ellez.piggywords.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: WordReview)

    @Query("SELECT * FROM word_reviews WHERE wordId = :wordId ORDER BY reviewDate DESC")
    fun getReviewsForWord(wordId: Int): Flow<List<WordReview>>

    @Query("SELECT * FROM word_reviews WHERE wordId = :wordId ORDER BY reviewDate DESC")
    suspend fun getReviewsForWordSync(wordId: Int): List<WordReview>

    @Query("SELECT COUNT(*) FROM word_reviews WHERE wordId = :wordId")
    suspend fun getReviewCount(wordId: Int): Int

    @Query("SELECT COUNT(*) FROM word_reviews WHERE wordId = :wordId AND isCorrect = 1")
    suspend fun getCorrectCount(wordId: Int): Int

    @Query("SELECT COUNT(*) FROM word_reviews WHERE reviewDate = :date")
    suspend fun getReviewCountForDate(date: String): Int

    @Query("DELETE FROM word_reviews WHERE wordId = :wordId")
    suspend fun deleteReviewsForWord(wordId: Int)
}