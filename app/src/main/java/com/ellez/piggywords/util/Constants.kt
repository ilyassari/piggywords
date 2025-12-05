package com.ellez.piggywords.util

/**
 * Word Status Constants
 *
 * Defines the learning status of a word based on review history
 *
 * Note: Suppressing "unused" warnings as these will be used after implementation
 */
@Suppress("unused")
object WordStatus {
    private const val MASTERY_LEVEL = 6  // Level 6 and above = mastered

    /**
     * Determine word status based on learning level and review history
     *
     * @param learningLevel Current learning level of the word
     * @param lastReviewedDate Last review date (null if never reviewed)
     * @return Status string: "NEW", "LEARNING", or "MASTERED"
     */
    fun getStatus(learningLevel: Int, lastReviewedDate: String?): String {
        return when {
            // Never reviewed = NEW
            lastReviewedDate == null -> "NEW"

            // Reviewed but not mastered = LEARNING
            learningLevel < MASTERY_LEVEL -> "LEARNING"

            // Mastered = MASTERED
            else -> "MASTERED"
        }
    }

    /**
     * Check if word is truly new (never reviewed before)
     */
    fun isNew(lastReviewedDate: String?): Boolean {
        return lastReviewedDate == null
    }

    /**
     * Check if word is in learning phase
     */
    fun isLearning(learningLevel: Int, lastReviewedDate: String?): Boolean {
        return lastReviewedDate != null && learningLevel < MASTERY_LEVEL
    }

    /**
     * Check if word is mastered
     */
    fun isMastered(learningLevel: Int): Boolean {
        return learningLevel >= MASTERY_LEVEL
    }
}

/**
 * Word Type Constants
 *
 * Defines 15 categories for word classification:
 * - Grammar categories (1-8): Noun, Verb, Adjective, etc.
 * - Usage/Style categories (9-15): Phrase, Idiom, Colloquial, etc.
 *
 * Each word can have multiple types (e.g., "buddy" can be both Noun and Slang)
 *
 * Note: Suppressing "unused" warnings as this will be used after migration
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object WordTypes {
    // Grammar Categories (1-8)
    const val NOUN = 1
    const val VERB = 2
    const val ADJECTIVE = 3
    const val ADVERB = 4
    const val PRONOUN = 5
    const val PREPOSITION = 6
    const val CONJUNCTION = 7
    const val INTERJECTION = 8

    // Usage/Style Categories (9-15)
    const val PHRASE = 9
    const val PHRASAL_VERB = 10
    const val IDIOM = 11
    const val COLLOQUIAL = 12
    const val SLANG = 13
    const val FORMAL = 14
    const val LITERARY = 15

    /**
     * Get display name for a type ID
     */
    fun getTypeName(type: Int): String {
        return when(type) {
            NOUN -> "Noun"
            VERB -> "Verb"
            ADJECTIVE -> "Adjective"
            ADVERB -> "Adverb"
            PRONOUN -> "Pronoun"
            PREPOSITION -> "Preposition"
            CONJUNCTION -> "Conjunction"
            INTERJECTION -> "Interjection"
            PHRASE -> "Phrase"
            PHRASAL_VERB -> "Phrasal Verb"
            IDIOM -> "Idiom"
            COLLOQUIAL -> "Colloquial"
            SLANG -> "Slang"
            FORMAL -> "Formal"
            LITERARY -> "Literary"
            else -> "Other"
        }
    }

    /**
     * Get type ID from string name
     */
    fun getTypeId(typeName: String): Int {
        return when(typeName.lowercase().trim()) {
            "noun" -> NOUN
            "verb" -> VERB
            "adjective" -> ADJECTIVE
            "adverb" -> ADVERB
            "pronoun" -> PRONOUN
            "preposition" -> PREPOSITION
            "conjunction" -> CONJUNCTION
            "interjection" -> INTERJECTION
            "phrase" -> PHRASE
            "phrasal verb", "phrasal_verb" -> PHRASAL_VERB
            "idiom" -> IDIOM
            "colloquial" -> COLLOQUIAL
            "slang" -> SLANG
            "formal" -> FORMAL
            "literary" -> LITERARY
            else -> 0
        }
    }

    /**
     * Convert list of type IDs to comma-separated string for database storage
     * Example: [1, 11] -> "1,11"
     */
    fun typeListToString(types: List<Int>): String {
        return types.sorted().joinToString(",")
    }

    /**
     * Convert comma-separated string to list of type IDs
     * Example: "1,11" -> [1, 11]
     */
    fun stringToTypeList(typeString: String): List<Int> {
        if (typeString.isBlank()) return emptyList()
        return typeString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..15 }
    }

    /**
     * Get display names for multiple types
     * Example: [1, 11] -> "Noun, Colloquial"
     */
    fun getTypeNames(types: List<Int>): String {
        return types.joinToString(", ") { getTypeName(it) }
    }

    /**
     * Get primary type (first grammar category if exists, otherwise first type)
     * Example: [11, 1] -> 1 (Noun is primary)
     * Example: [11, 12] -> 11 (Colloquial is primary)
     */
    fun getPrimaryType(types: List<Int>): Int {
        if (types.isEmpty()) return 0

        // Grammar categories (1-8) take precedence
        val grammarType = types.firstOrNull { it in 1..8 }
        return grammarType ?: types.first()
    }

    /**
     * Check if types contain a specific category
     */
    fun hasType(types: List<Int>, type: Int): Boolean {
        return types.contains(type)
    }

    /**
     * Get all available type IDs with names
     */
    fun getAllTypes(): Map<Int, String> {
        return mapOf(
            NOUN to "Noun",
            VERB to "Verb",
            ADJECTIVE to "Adjective",
            ADVERB to "Adverb",
            PRONOUN to "Pronoun",
            PREPOSITION to "Preposition",
            CONJUNCTION to "Conjunction",
            INTERJECTION to "Interjection",
            PHRASE to "Phrase",
            PHRASAL_VERB to "Phrasal Verb",
            IDIOM to "Idiom",
            COLLOQUIAL to "Colloquial",
            SLANG to "Slang",
            FORMAL to "Formal",
            LITERARY to "Literary"
        )
    }

    /**
     * Get icon/emoji for type (optional, for UI)
     */
    fun getTypeIcon(type: Int): String {
        return when(type) {
            NOUN -> "📦"
            VERB -> "⚡"
            ADJECTIVE -> "🎨"
            ADVERB -> "🔄"
            PRONOUN -> "👤"
            PREPOSITION -> "🔗"
            CONJUNCTION -> "🔗"
            INTERJECTION -> "💬"
            PHRASE -> "💭"
            PHRASAL_VERB -> "🔀"
            IDIOM -> "🎭"
            COLLOQUIAL -> "🗣️"
            SLANG -> "😎"
            FORMAL -> "🎩"
            LITERARY -> "📚"
            else -> "❓"
        }
    }
}