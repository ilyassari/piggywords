package com.ellez.piggywords.data.model

import com.ellez.piggywords.util.WordTypes

data class ExampleWord(
    val level: String,
    val word: String,
    val type: List<Int>,  // Changed from List<String> to List<Int> to match JSON format
    val definition: String,
    val translation: Map<String, String>,
    val sentence: String? = null  // Optional sentence field from JSON
) {
    /**
     * Combines type IDs into display names
     * Example: [1, 11] -> "Noun, Idiom"
     */
    fun getTypeString(): String {
        return WordTypes.getTypeNames(type)
    }

    /**
     * Gets translation for the specified language
     * Returns empty string if language not found or if "other" is selected
     */
    fun getTranslationForLanguage(language: String): String {
        return translation[language.lowercase()] ?: ""
    }

    /**
     * Get types as comma-separated string for database storage
     * Example: [1, 11] -> "1,11"
     */
    @Suppress("unused")
    fun getTypeIdsString(): String {
        return WordTypes.typeListToString(type)
    }
}

@Suppress("unused")
data class ExampleWordList(
    val words: List<ExampleWord>
)