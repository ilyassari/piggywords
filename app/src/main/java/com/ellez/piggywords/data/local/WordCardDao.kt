package com.ellez.piggywords.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordCard)

    @Update
    suspend fun updateWord(word: WordCard)

    @Delete
    suspend fun deleteWord(word: WordCard)

    @Query("SELECT * FROM word_cards WHERE id = :id")
    suspend fun getWordById(id: Int): WordCard?

    @Query("SELECT * FROM word_cards ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<WordCard>>

    // NEW: Synchronous version for duplicate checking
    @Query("SELECT * FROM word_cards ORDER BY createdAt DESC")
    suspend fun getAllWordsSync(): List<WordCard>

    // Filter by word type
    @Query("SELECT * FROM word_cards WHERE wordType = :wordType ORDER BY createdAt DESC")
    fun getWordsByType(wordType: String): Flow<List<WordCard>>

    // Get words to study (NEW LOGIC)
    // Priority order:
    // 1. scheduledDate (older dates first)
    // 2. learningLevel (higher level first)
    // 3. createdAt (older additions first)
    // CRITICAL FIX: Exclude words already reviewed today to prevent duplicate reviews
    @Query("""
        SELECT * FROM word_cards 
        WHERE isRetired = 0 
        AND scheduledDate <= :today
        AND (lastReviewedDate IS NULL OR lastReviewedDate < :today)
        ORDER BY scheduledDate ASC, learningLevel DESC, createdAt ASC 
        LIMIT :limit
    """)
    fun getWordsForStudy(today: String, limit: Int): Flow<List<WordCard>>

    // Total number of words that should be studied today
    @Query("SELECT COUNT(*) FROM word_cards WHERE isRetired = 0 AND scheduledDate <= :today")
    suspend fun getPendingWordCount(today: String): Int

    @Query("UPDATE word_cards SET isRetired = 1 WHERE id = :id")
    suspend fun retireWord(id: Int)
}