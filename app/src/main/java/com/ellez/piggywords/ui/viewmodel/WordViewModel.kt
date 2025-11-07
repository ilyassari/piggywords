package com.ellez.piggywords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ellez.piggywords.data.local.AppDatabase
import com.ellez.piggywords.data.local.WordCard
import com.ellez.piggywords.data.local.WordReview
import com.ellez.piggywords.data.local.getCurrentDateString
import com.ellez.piggywords.data.repository.WordRepository
import com.ellez.piggywords.util.SettingsManager
import com.ellez.piggywords.util.WordPoolManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class WordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WordRepository
    private val settingsManager: SettingsManager
    private val wordPoolManager: WordPoolManager

    // Settings
    private val _dailyWordLimit = MutableStateFlow(30)
    val dailyWordLimit: StateFlow<Int> = _dailyWordLimit.asStateFlow()

    private val _defaultFrequencyMultiplier = MutableStateFlow(2)
    val defaultFrequencyMultiplier: StateFlow<Int> = _defaultFrequencyMultiplier.asStateFlow()

    // Words
    val allWords: StateFlow<List<WordCard>>
    val studyWords: StateFlow<List<WordCard>>

    // Counters
    private val _todayStudiedCount = MutableStateFlow(0)
    val todayStudiedCount: StateFlow<Int> = _todayStudiedCount.asStateFlow()

    private val _pendingWordCount = MutableStateFlow(0)
    val pendingWordCount: StateFlow<Int> = _pendingWordCount.asStateFlow()

    // Selected word type for filtering
    private val _selectedWordType = MutableStateFlow<String?>(null)
    private val selectedWordType: StateFlow<String?> = _selectedWordType.asStateFlow()

    // Filtered words
    private val filteredWords: StateFlow<List<WordCard>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WordRepository(db.wordCardDao(), db.wordReviewDao())
        settingsManager = SettingsManager(application)
        wordPoolManager = WordPoolManager(application)

        // Load saved settings from SharedPreferences
        _dailyWordLimit.value = settingsManager.dailyWordLimit
        _defaultFrequencyMultiplier.value = settingsManager.frequencyMultiplier

        // Get all words as a Flow
        allWords = repository.getAllWords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Get words to study (new logic)
        studyWords = dailyWordLimit.flatMapLatest { limit ->
            repository.getWordsForStudy(limit)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Filtered words
        filteredWords = combine(allWords, selectedWordType) { words, type ->
            if (type == null) words
            else words.filter { it.wordType == type }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Update counters
        viewModelScope.launch {
            updateCounters()
        }
    }

    fun insertWord(
        word: String,
        wordType: String,
        definition: String,
        translation: String,
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            val todayString = getCurrentDateString()
            val newWord = WordCard(
                word = word,
                wordType = wordType,
                definition = definition,
                translation = translation,
                imagePath = imagePath,
                learningLevel = 0,
                nextReviewDate = todayString,
                scheduledDate = todayString,
                frequencyMultiplier = _defaultFrequencyMultiplier.value
            )
            repository.insertWord(newWord)
            updateCounters()
        }
    }

    // Note: updateWord is kept for future edit functionality
    @Suppress("unused")
    fun updateWord(word: WordCard) {
        viewModelScope.launch {
            repository.updateWord(word)
        }
    }

    fun deleteWord(word: WordCard) {
        viewModelScope.launch {
            repository.deleteWord(word)
            updateCounters()
        }
    }

    fun answerWord(wordId: Int, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.updateWordAfterReview(wordId, isCorrect)
            updateCounters()
        }
    }

    // Note: retireWord is kept for future "master word" functionality
    @Suppress("unused")
    fun retireWord(wordId: Int) {
        viewModelScope.launch {
            repository.retireWord(wordId)
            updateCounters()
        }
    }

    fun updateDailyWordLimit(limit: Int) {
        _dailyWordLimit.value = limit
        // Save to SharedPreferences
        settingsManager.dailyWordLimit = limit
        // Update counters when limit changes
        viewModelScope.launch {
            updateCounters()
        }
    }

    fun updateDefaultFrequencyMultiplier(multiplier: Int) {
        _defaultFrequencyMultiplier.value = multiplier
        // Save to SharedPreferences
        settingsManager.frequencyMultiplier = multiplier
    }

    // Note: setWordTypeFilter is kept for future filtering functionality
    @Suppress("unused")
    fun setWordTypeFilter(wordType: String?) {
        _selectedWordType.value = wordType
    }

    // Get review statistics
    fun getReviewsForWord(wordId: Int): Flow<List<WordReview>> {
        return repository.getReviewsForWord(wordId)
    }

    fun getReviewStats(wordId: Int): Pair<Int, Int> {
        // Note: This should be called from a coroutine scope
        return runBlocking {
            repository.getReviewStats(wordId)
        }
    }

    private suspend fun updateCounters() {
        _todayStudiedCount.value = repository.getTodayStudiedCount()

        // ✅ CORRECT LOGIC: Badge shows minimum of:
        // 1. Words scheduled for today (pending words)
        // 2. Remaining capacity in daily limit

        val todayStudied = _todayStudiedCount.value
        val dailyLimit = _dailyWordLimit.value
        val actualPendingWords = repository.getPendingWordCount() // Words due today

        // Calculate remaining capacity
        val remainingCapacity = (dailyLimit - todayStudied).coerceAtLeast(0)

        // Badge = min(actual pending words, remaining capacity)
        // Example 1: pending=5, limit=8, studied=0 → badge=5 (not 8!)
        // Example 2: pending=13, limit=10, studied=4 → badge=6 (not 13!)
        // Example 3: pending=5, limit=10, studied=10 → badge=0 (limit reached)
        // Example 4: pending=0, limit=10, studied=5 → badge=0 (no words to study)
        _pendingWordCount.value = minOf(actualPendingWords, remainingCapacity)
    }

    // NEW: Pool Management Functions
    suspend fun getRandomWordsFromPool(count: Int): List<com.ellez.piggywords.data.model.ExampleWord> {
        val levels = settingsManager.getLevelsForUserLevel()
        val existingWords = repository.getAllExistingWords()
        return wordPoolManager.getRandomWordsByLevel(levels, count, existingWords)
    }

    fun searchWordInPool(query: String): com.ellez.piggywords.data.model.ExampleWord? {
        return runBlocking {
            wordPoolManager.searchWordInPool(query)
        }
    }

    suspend fun checkWordExists(word: String): Boolean {
        return repository.isWordExists(word)
    }

    // NEW: Settings Management Functions
    fun getUserLevel(): String = settingsManager.userLevel

    fun getTranslationLanguage(): String = settingsManager.translationLanguage

    fun setUserLevel(level: String) {
        settingsManager.userLevel = level
    }

    fun setTranslationLanguage(language: String) {
        settingsManager.translationLanguage = language
    }

    fun getAllLanguages(): List<String> = settingsManager.getAllLanguages()

    fun getLanguageDisplayName(language: String): String =
        settingsManager.getLanguageDisplayName(language)

    // Study Screen Visibility Settings
    fun getShowWordTypeInQuestion(): Boolean = settingsManager.showWordTypeInQuestion

    fun setShowWordTypeInQuestion(show: Boolean) {
        settingsManager.showWordTypeInQuestion = show
    }

    fun getShowDefinitionInQuestion(): Boolean = settingsManager.showDefinitionInQuestion

    fun setShowDefinitionInQuestion(show: Boolean) {
        settingsManager.showDefinitionInQuestion = show
    }
}