package com.ellez.piggywords.ui.screens

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ellez.piggywords.data.local.WordCard
import com.ellez.piggywords.ui.viewmodel.WordViewModel
import com.ellez.piggywords.util.WordTypes
import java.util.Locale

enum class WordTypeFilter {
    ALL, NOUN, VERB, ADJECTIVE, ADVERB, PHRASE, OTHER
}

enum class LevelFilter {
    ALL, NEW, LEARNING, MASTERED
}

enum class SortOption {
    ALPHABETICAL, NEWEST, LEVEL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    viewModel: WordViewModel,
    onNavigateToWordDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    initialTypeFilter: String? = null,
    initialLevelFilter: String? = null
) {
    val context = LocalContext.current
    val allWords by viewModel.allWords.collectAsState()

    // TTS initialization
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }

    // Parse initial filters
    val initialType = when (initialTypeFilter?.uppercase()) {
        "NOUN" -> WordTypeFilter.NOUN
        "VERB" -> WordTypeFilter.VERB
        "ADJECTIVE" -> WordTypeFilter.ADJECTIVE
        "ADVERB" -> WordTypeFilter.ADVERB
        "PHRASE" -> WordTypeFilter.PHRASE
        else -> WordTypeFilter.ALL
    }

    val initialLevel = when (initialLevelFilter?.uppercase()) {
        "NEW" -> LevelFilter.NEW
        "LEARNING" -> LevelFilter.LEARNING
        "MASTERED" -> LevelFilter.MASTERED
        "PENDING" -> LevelFilter.ALL // Will be handled separately
        else -> LevelFilter.ALL
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf(initialType) }
    var selectedLevelFilter by remember { mutableStateOf(initialLevel) }
    var sortOption by remember { mutableStateOf(SortOption.ALPHABETICAL) }
    var showSortMenu by remember { mutableStateOf(false) }
    val showPendingOnly by remember { mutableStateOf(initialLevelFilter?.uppercase() == "PENDING") }

    var selectedWord by remember { mutableStateOf<WordCard?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Close bottom sheet when delete dialog opens
    LaunchedEffect(showDeleteDialog) {
        if (showDeleteDialog) {
            showBottomSheet = false
        }
    }

    // Filter and sort words
    val filteredWords = remember(allWords, searchQuery, selectedTypeFilter, selectedLevelFilter, sortOption, showPendingOnly) {
        allWords
            .filter { word ->
                // Parse word types from string (e.g., "1,11" -> [1, 11])
                val wordTypeIds = WordTypes.stringToTypeList(word.wordType)

                // Search filter
                val matchesSearch = searchQuery.isBlank() ||
                        word.word.contains(searchQuery, ignoreCase = true) ||
                        word.translation.contains(searchQuery, ignoreCase = true) ||
                        word.definition.contains(searchQuery, ignoreCase = true)

                // Pending filter (words due for review today)
                val matchesPending = if (showPendingOnly) {
                    val today = com.ellez.piggywords.data.local.getCurrentDateString()
                    word.nextReviewDate <= today
                } else {
                    true
                }

                // Type filter - now using integer IDs
                val matchesType = when (selectedTypeFilter) {
                    WordTypeFilter.ALL -> true
                    WordTypeFilter.NOUN -> wordTypeIds.contains(WordTypes.NOUN)
                    WordTypeFilter.VERB -> wordTypeIds.contains(WordTypes.VERB)
                    WordTypeFilter.ADJECTIVE -> wordTypeIds.contains(WordTypes.ADJECTIVE)
                    WordTypeFilter.ADVERB -> wordTypeIds.contains(WordTypes.ADVERB)
                    WordTypeFilter.PHRASE -> wordTypeIds.contains(WordTypes.PHRASE)
                    WordTypeFilter.OTHER -> {
                        // Words that don't contain any of the main grammar categories
                        !wordTypeIds.any { it in listOf(
                            WordTypes.NOUN,
                            WordTypes.VERB,
                            WordTypes.ADJECTIVE,
                            WordTypes.ADVERB,
                            WordTypes.PHRASE
                        )}
                    }
                }

                // Level filter
                val matchesLevel = when (selectedLevelFilter) {
                    LevelFilter.ALL -> true
                    LevelFilter.NEW -> word.learningLevel == 0
                    LevelFilter.LEARNING -> word.learningLevel in 1..5
                    LevelFilter.MASTERED -> word.learningLevel >= 6
                }

                matchesSearch && matchesPending && matchesType && matchesLevel
            }
            .let { filtered ->
                when (sortOption) {
                    SortOption.ALPHABETICAL -> filtered.sortedBy { it.word.lowercase() }
                    SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
                    SortOption.LEVEL -> filtered.sortedByDescending { it.learningLevel }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                showPendingOnly -> "Words to Review"
                                selectedLevelFilter == LevelFilter.NEW -> "New Words"
                                selectedLevelFilter == LevelFilter.LEARNING -> "Learning Words"
                                selectedLevelFilter == LevelFilter.MASTERED -> "Mastered Words"
                                else -> "My Words"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${filteredWords.size} of ${allWords.size} words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Sort button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("A-Z") },
                                onClick = {
                                    sortOption = SortOption.ALPHABETICAL
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.SortByAlpha, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    sortOption = SortOption.NEWEST
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.NewReleases, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Level") },
                                onClick = {
                                    sortOption = SortOption.LEVEL
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search words...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Filter Chips
            ScrollableChipRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Type Filters
                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.ALL,
                    onClick = { selectedTypeFilter = WordTypeFilter.ALL },
                    label = { Text("All Types") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.ALL) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.NOUN,
                    onClick = { selectedTypeFilter = WordTypeFilter.NOUN },
                    label = { Text("Noun") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.NOUN) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.VERB,
                    onClick = { selectedTypeFilter = WordTypeFilter.VERB },
                    label = { Text("Verb") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.VERB) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.ADJECTIVE,
                    onClick = { selectedTypeFilter = WordTypeFilter.ADJECTIVE },
                    label = { Text("Adjective") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.ADJECTIVE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.ADVERB,
                    onClick = { selectedTypeFilter = WordTypeFilter.ADVERB },
                    label = { Text("Adverb") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.ADVERB) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.PHRASE,
                    onClick = { selectedTypeFilter = WordTypeFilter.PHRASE },
                    label = { Text("Phrase") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.PHRASE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedTypeFilter == WordTypeFilter.OTHER,
                    onClick = { selectedTypeFilter = WordTypeFilter.OTHER },
                    label = { Text("Other") },
                    leadingIcon = if (selectedTypeFilter == WordTypeFilter.OTHER) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            // Level Filters
            if (!showPendingOnly) {
                ScrollableChipRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    FilterChip(
                        selected = selectedLevelFilter == LevelFilter.ALL,
                        onClick = { selectedLevelFilter = LevelFilter.ALL },
                        label = { Text("All Levels") },
                        leadingIcon = if (selectedLevelFilter == LevelFilter.ALL) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = selectedLevelFilter == LevelFilter.NEW,
                        onClick = { selectedLevelFilter = LevelFilter.NEW },
                        label = { Text("New") },
                        leadingIcon = if (selectedLevelFilter == LevelFilter.NEW) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = selectedLevelFilter == LevelFilter.LEARNING,
                        onClick = { selectedLevelFilter = LevelFilter.LEARNING },
                        label = { Text("Learning") },
                        leadingIcon = if (selectedLevelFilter == LevelFilter.LEARNING) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = selectedLevelFilter == LevelFilter.MASTERED,
                        onClick = { selectedLevelFilter = LevelFilter.MASTERED },
                        label = { Text("Mastered") },
                        leadingIcon = if (selectedLevelFilter == LevelFilter.MASTERED) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Word List
            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No words found",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isNotEmpty() || selectedTypeFilter != WordTypeFilter.ALL || selectedLevelFilter != LevelFilter.ALL) {
                            Text(
                                text = "Try adjusting your filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredWords,
                        key = { it.id }
                    ) { word ->
                        WordListItem(
                            word = word,
                            onClick = { onNavigateToWordDetail(word.id) },
                            onLongClick = {
                                selectedWord = word
                                showBottomSheet = true
                            },
                            tts = tts,
                            ttsReady = ttsReady,
                            context = context
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for Word Options
    if (showBottomSheet && selectedWord != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedWord = null
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Text(
                    text = selectedWord!!.word,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider()

                // Edit Option
                ListItem(
                    headlineContent = { Text("Edit Word") },
                    leadingContent = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        showBottomSheet = false
                        onNavigateToWordDetail(selectedWord!!.id)
                        selectedWord = null
                    }
                )

                // Delete Option
                ListItem(
                    headlineContent = { Text("Delete Word") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable {
                        showDeleteDialog = true
                    },
                    colors = ListItemDefaults.colors(
                        headlineColor = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog && selectedWord != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                showBottomSheet = false
            },
            title = { Text("Delete Word?") },
            text = {
                Text("Are you sure you want to delete \"${selectedWord!!.word}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWord(selectedWord!!)
                        showDeleteDialog = false
                        showBottomSheet = false
                        selectedWord = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        showBottomSheet = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordListItem(
    word: WordCard,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    tts: TextToSpeech? = null,
    ttsReady: Boolean = false,
    context: android.content.Context? = null
) {
    // Convert wordType string (e.g., "1,11") to display names (e.g., "Noun, Idiom")
    val typeIds = WordTypes.stringToTypeList(word.wordType)
    val displayType = WordTypes.getTypeNames(typeIds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Word Type Badge - NOW SHOWS PROPER NAMES INSTEAD OF NUMBERS
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = displayType,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    text = word.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = word.definition,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action buttons
            if (context != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Listen button
                    IconButton(
                        onClick = {
                            if (ttsReady) {
                                tts?.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // YouGlish button
                    IconButton(
                        onClick = {
                            val url = "https://youglish.com/pronounce/${word.word}/english"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "YouGlish",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Level Badge
            LevelBadge(learningLevel = word.learningLevel)
        }
    }
}

@Composable
fun LevelBadge(learningLevel: Int) {
    val (label, color) = when (learningLevel) {
        0 -> "New" to Color(0xFFFF9800)
        in 1..5 -> "Learning" to MaterialTheme.colorScheme.primary
        else -> "Mastered" to MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (learningLevel) {
                    0 -> Icons.Default.FiberNew
                    in 1..5 -> Icons.Default.School
                    else -> Icons.Default.Star
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ScrollableChipRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}