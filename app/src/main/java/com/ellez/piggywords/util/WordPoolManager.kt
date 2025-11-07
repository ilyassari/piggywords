package com.ellez.piggywords.util

import android.content.Context
import android.util.Log
import com.ellez.piggywords.data.model.ExampleWord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WordPoolManager(private val context: Context) {

    private var cachedWords: List<ExampleWord>? = null

    companion object {
        private const val TAG = "WordPoolManager"
    }

    // Loads all words from the JSON file in assets
    // Caches the result to avoid repeated file reads
    private fun loadWordsFromAssets(): List<ExampleWord> {
        if (cachedWords != null) {
            Log.d(TAG, "Returning cached words: ${cachedWords!!.size}")
            return cachedWords!!
        }

        return try {
            Log.d(TAG, "Loading words from assets...")
            val jsonString = context.assets.open("example_words.json")
                .bufferedReader()
                .use { it.readText() }

            Log.d(TAG, "JSON file read successfully, length: ${jsonString.length}")

            val gson = Gson()
            val listType = object : TypeToken<List<ExampleWord>>() {}.type
            val words: List<ExampleWord> = gson.fromJson(jsonString, listType)

            Log.d(TAG, "Successfully loaded ${words.size} words from JSON")

            // Log first word for verification
            if (words.isNotEmpty()) {
                val firstWord = words.first()
                Log.d(TAG, "First word: ${firstWord.word}, type: ${firstWord.type}, level: ${firstWord.level}")
            }

            cachedWords = words
            words
        } catch (e: Exception) {
            Log.e(TAG, "ERROR loading words: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    // Gets random words filtered by level and excludes already added words
    fun getRandomWordsByLevel(
        levels: List<String>,
        count: Int,
        excludeWords: List<String>
    ): List<ExampleWord> {
        Log.d(TAG, "getRandomWordsByLevel - levels: $levels, count: $count, excludeCount: ${excludeWords.size}")

        val allWords = loadWordsFromAssets()
        Log.d(TAG, "Total words loaded: ${allWords.size}")

        val filteredWords = allWords
            .filter { it.level in levels }
            .filter { it.word.lowercase() !in excludeWords.map { word -> word.lowercase() } }

        Log.d(TAG, "Filtered words count: ${filteredWords.size}")

        val result = if (filteredWords.size <= count) {
            filteredWords
        } else {
            filteredWords.shuffled().take(count)
        }

        Log.d(TAG, "Returning ${result.size} words")
        return result
    }

    // Searches for a specific word in the pool (case-insensitive)
    fun searchWordInPool(query: String): ExampleWord? {
        Log.d(TAG, "Searching for word: $query")
        val allWords = loadWordsFromAssets()
        val found = allWords.find {
            it.word.equals(query, ignoreCase = true)
        }
        Log.d(TAG, "Search result: ${if (found != null) "Found" else "Not found"}")
        return found
    }

    // Gets all words for specific levels
    @Suppress("unused")
    fun getWordsByLevel(levels: List<String>): List<ExampleWord> {
        Log.d(TAG, "getWordsByLevel - levels: $levels")
        val allWords = loadWordsFromAssets()
        val filtered = allWords.filter { it.level in levels }
        Log.d(TAG, "Found ${filtered.size} words for levels: $levels")
        return filtered
    }
}