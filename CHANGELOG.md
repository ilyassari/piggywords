# Changelog

All notable changes to PiggyWords will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Planned
- Push notifications for daily reminders
- Export/Import backup functionality
- Basic example sentences
- Flashcard word images
- Dark/Light theme support

## [0.1.0] - 2025-11-08
### Added
- Initial beta release
- Spaced repetition learning algorithm (2^learningLevel days)
- Daily study limit and tracking system
- Word management features:
    - Add new words with required example sentences
    - Edit existing words
    - Delete words
    - Add images from gallery
- Flash card study mode with flip animation
- Multiple word types support (15 types):
    - Noun, Verb, Adjective, Adverb, Pronoun, Preposition, Conjunction
    - Interjection, Phrase, Phrasal Verb, Idiom, Colloquial, Slang, Formal, Literary
- Offline-first architecture (no internet required)
- Word filtering by:
    - Word type
- Progress tracking and statistics:
    - Total words studied
    - Daily completion percentage
- Review pool management:
    - Priority system (review pool → new words pool)
    - Next review date calculation
- Translation support for 9 languages:
    - Turkish, Portuguese, Russian, Chinese, Japanese, Arabic, Spanish, Hindi, Indonesian
    - Pool words can added with translation
- Room Database for local data persistence

### Technical Details
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34
- Compile SDK: 35
- 100% Jetpack Compose UI (no XML)
- MVVM architecture pattern
- Kotlin 2.0.0
- Material Design 3 theming

### Known Issues
- No cloud backup functionality yet
- No push notification system
- No TTS (Text-to-Speech) for example sentences
- Limited to English vocabulary learning only
- No widget support
- No social sharing features

### Beta Testing Notes
This is a beta version (0.1.0). The app is stable for daily use but some features are still under development. Please report any bugs or suggestions via GitHub Issues.

---

## Version History

| Version | Release Date | Type | Highlights |
|---------|--------------|------|------------|
| 0.1.0 | 2025-11-08 | Beta | Initial public beta release |

---

## Feedback & Bug Reports

Found a bug or have a suggestion? Please open an issue on GitHub:
https://github.com/ilyassari/piggywords/issues

We appreciate your feedback during the beta phase!