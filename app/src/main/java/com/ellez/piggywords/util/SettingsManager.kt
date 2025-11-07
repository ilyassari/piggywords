package com.ellez.piggywords.util

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "piggy_words_settings",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_USER_LEVEL = "user_level"
        private const val KEY_TRANSLATION_LANGUAGE = "translation_language"
        private const val KEY_DAILY_WORD_LIMIT = "daily_word_limit"
        private const val KEY_FREQUENCY_MULTIPLIER = "frequency_multiplier"
        private const val KEY_SHOW_WORD_TYPE_IN_QUESTION = "show_word_type_in_question"
        private const val KEY_SHOW_DEFINITION_IN_QUESTION = "show_definition_in_question"

        const val LEVEL_BEGINNER = "beginner"
        const val LEVEL_INTERMEDIATE = "intermediate"
        const val LEVEL_ADVANCED = "advanced"

        const val LANG_TURKISH = "turkish"
        const val LANG_PORTUGUESE = "portuguese"
        const val LANG_RUSSIAN = "russian"
        const val LANG_CHINESE = "chinese"
        const val LANG_JAPANESE = "japanese"
        const val LANG_ARABIC = "arabic"
        const val LANG_SPANISH = "spanish"
        const val LANG_HINDI = "hindi"
        const val LANG_INDONESIAN = "indonesian"
        const val LANG_OTHER = "other"

        const val DEFAULT_DAILY_LIMIT = 30
        const val DEFAULT_FREQUENCY_MULTIPLIER = 2
    }

    // User level property with getter and setter
    var userLevel: String
        get() = prefs.getString(KEY_USER_LEVEL, LEVEL_BEGINNER) ?: LEVEL_BEGINNER
        set(value) = prefs.edit().putString(KEY_USER_LEVEL, value).apply()

    // Translation language property with getter and setter
    var translationLanguage: String
        get() = prefs.getString(KEY_TRANSLATION_LANGUAGE, LANG_TURKISH) ?: LANG_TURKISH
        set(value) = prefs.edit().putString(KEY_TRANSLATION_LANGUAGE, value).apply()

    // Daily word limit property with getter and setter
    var dailyWordLimit: Int
        get() = prefs.getInt(KEY_DAILY_WORD_LIMIT, DEFAULT_DAILY_LIMIT)
        set(value) = prefs.edit().putInt(KEY_DAILY_WORD_LIMIT, value).apply()

    // Frequency multiplier property with getter and setter
    var frequencyMultiplier: Int
        get() = prefs.getInt(KEY_FREQUENCY_MULTIPLIER, DEFAULT_FREQUENCY_MULTIPLIER)
        set(value) = prefs.edit().putInt(KEY_FREQUENCY_MULTIPLIER, value).apply()

    // Show word type in question (default: true)
    var showWordTypeInQuestion: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WORD_TYPE_IN_QUESTION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WORD_TYPE_IN_QUESTION, value).apply()

    // Show definition in question (default: false)
    var showDefinitionInQuestion: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DEFINITION_IN_QUESTION, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DEFINITION_IN_QUESTION, value).apply()

    // Returns CEFR levels based on user's selected level
    fun getLevelsForUserLevel(): List<String> {
        return when (userLevel) {
            LEVEL_BEGINNER -> listOf("A1", "A2", "B1")
            LEVEL_INTERMEDIATE -> listOf("B1", "B2")
            LEVEL_ADVANCED -> listOf("B2", "C1", "C2")
            else -> listOf("A1", "A2", "B1")
        }
    }

    // Returns all available languages
    fun getAllLanguages(): List<String> {
        return listOf(
            LANG_TURKISH,
            LANG_PORTUGUESE,
            LANG_RUSSIAN,
            LANG_CHINESE,
            LANG_JAPANESE,
            LANG_ARABIC,
            LANG_SPANISH,
            LANG_HINDI,
            LANG_INDONESIAN,
            LANG_OTHER
        )
    }

    // Returns display name for a language code
    fun getLanguageDisplayName(language: String): String {
        return when (language) {
            LANG_TURKISH -> "Turkish"
            LANG_PORTUGUESE -> "Portuguese"
            LANG_RUSSIAN -> "Russian"
            LANG_CHINESE -> "Chinese"
            LANG_JAPANESE -> "Japanese"
            LANG_ARABIC -> "Arabic"
            LANG_SPANISH -> "Spanish"
            LANG_HINDI -> "Hindi"
            LANG_INDONESIAN -> "Indonesian"
            LANG_OTHER -> "Other"
            else -> language.replaceFirstChar { it.uppercase() }
        }
    }
}