package com.ellez.piggywords.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
// Removed import java.time.LocalDate to resolve API 26 error

@Database(
    entities = [WordCard::class, WordReview::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordCardDao(): WordCardDao
    abstract fun wordReviewDao(): WordReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration 1 -> 2: Add new columns and table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the WordReview table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS word_reviews (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        wordId INTEGER NOT NULL,
                        reviewDate TEXT NOT NULL,
                        isCorrect INTEGER NOT NULL,
                        learningLevel INTEGER NOT NULL,
                        createdAt TEXT NOT NULL
                    )
                """)

                // Define a static placeholder date to avoid the API 26 error.
                // This date is immediately overwritten by the UPDATE statement below.
                val placeholderDate = "2000-01-01"

                // Add new columns to the WordCard table
                database.execSQL("""
                    ALTER TABLE word_cards 
                    ADD COLUMN scheduledDate TEXT NOT NULL DEFAULT '$placeholderDate'
                """)

                database.execSQL("""
                    ALTER TABLE word_cards 
                    ADD COLUMN lastReviewedDate TEXT DEFAULT NULL
                """)

                database.execSQL("""
                    ALTER TABLE word_cards 
                    ADD COLUMN totalReviews INTEGER NOT NULL DEFAULT 0
                """)

                // Synchronize the scheduledDate of existing words with nextReviewDate
                database.execSQL("""
                    UPDATE word_cards 
                    SET scheduledDate = nextReviewDate
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "piggy_words_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // For development phase; may be removed in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}