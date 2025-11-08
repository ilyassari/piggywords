package com.ellez.piggywords.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ellez.piggywords.data.model.ExampleWord
import com.ellez.piggywords.ui.viewmodel.WordViewModel
import com.ellez.piggywords.util.WordTypes
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)

@Composable
fun AddWordScreen(
    viewModel: WordViewModel,
    onWordAdded: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    var selectedWordTypes by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var definition by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var showLoadPoolDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showDuplicateWarning by remember { mutableStateOf(false) }

    // Get all word types from WordTypes utility
    val wordTypesMap = remember { WordTypes.getAllTypes() }
    val isFormValid = word.isNotBlank() && selectedWordTypes.isNotEmpty() &&
            definition.isNotBlank() && translation.isNotBlank()

    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add New Word",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Expand your vocabulary!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Load from Pool Button
            OutlinedButton(
                onClick = { showLoadPoolDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.LibraryAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Load from Word Pool")
            }

            // Word Input Field with Search
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("Word") },
                placeholder = { Text("e.g., serendipity") },
                leadingIcon = {
                    Icon(Icons.Default.Abc, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (word.isNotBlank()) {
                                scope.launch {
                                    val result = viewModel.searchWordInPool(word.trim())
                                    if (result != null) {
                                        selectedWordTypes = result.type.toSet()
                                        definition = result.definition
                                        translation = result.getTranslationForLanguage(viewModel.getTranslationLanguage())
                                    } else {
                                        showSearchDialog = true
                                    }
                                }
                            } else {
                                showSearchDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search in pool",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Word Type Selection with Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Word Type",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Text(
                            text = "(Select one or more)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    // All word types in one FlowRow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..15).forEach { typeId ->
                            val typeName = wordTypesMap[typeId] ?: ""
                            FilterChip(
                                selected = selectedWordTypes.contains(typeId),
                                onClick = {
                                    selectedWordTypes = if (selectedWordTypes.contains(typeId)) {
                                        selectedWordTypes - typeId
                                    } else {
                                        selectedWordTypes + typeId
                                    }
                                },
                                label = { Text(typeName) },
                                leadingIcon = null,
                                modifier = Modifier.height(32.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.ellez.piggywords.ui.theme.TealDark,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }

                    // Show selected types
                    if (selectedWordTypes.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "Selected: ${WordTypes.getTypeNames(selectedWordTypes.toList())}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Definition Field
            ModernTextField(
                value = definition,
                onValueChange = { definition = it },
                label = "Definition",
                placeholder = "What does it mean?",
                icon = Icons.Default.Description,
                minLines = 2
            )

            // Translation Field
            ModernTextField(
                value = translation,
                onValueChange = { translation = it },
                label = "Translation (${viewModel.getLanguageDisplayName(viewModel.getTranslationLanguage())})",
                placeholder = "Translation in your language",
                icon = Icons.Default.Translate,
                minLines = 1
            )

            // Image Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Image (Optional)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    selectedImageUri?.let { uri ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove image",
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedImageUri != null) "Change Image" else "Add Image")
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        val wordExists = viewModel.checkWordExists(word.trim())
                        if (wordExists) {
                            showDuplicateWarning = true
                        } else {
                            // Convert selected type IDs to comma-separated string
                            val typeString = WordTypes.typeListToString(selectedWordTypes.toList())

                            viewModel.insertWord(
                                word = word.trim(),
                                wordType = typeString,
                                definition = definition.trim(),
                                translation = translation.trim(),
                                imagePath = selectedImageUri?.toString()
                            )
                            onWordAdded()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Save Word",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialogs
    if (showLoadPoolDialog) {
        LoadFromPoolDialog(
            viewModel = viewModel,
            onDismiss = { showLoadPoolDialog = false },
            onWordsSelected = { words ->
                showLoadPoolDialog = false
                // Add all selected words directly to database
                scope.launch {
                    words.forEach { selectedWord ->
                        // Check if word already exists
                        val wordExists = viewModel.checkWordExists(selectedWord.word.trim())
                        if (!wordExists) {
                            // Convert type IDs to comma-separated string
                            val typeString = WordTypes.typeListToString(selectedWord.type)

                            viewModel.insertWord(
                                word = selectedWord.word.trim(),
                                wordType = typeString,
                                definition = selectedWord.definition.trim(),
                                translation = selectedWord.getTranslationForLanguage(viewModel.getTranslationLanguage()).trim(),
                                imagePath = null
                            )
                        }
                    }
                    // Notify user about added words
                    if (words.isNotEmpty()) {
                        // Words added successfully - could show a toast here
                        onWordAdded()
                    }
                }
            }
        )
    }

    if (showSearchDialog) {
        SearchInPoolDialog(
            viewModel = viewModel,
            onDismiss = { showSearchDialog = false },
            onWordSelected = { selectedWord ->
                showSearchDialog = false
                word = selectedWord.word
                selectedWordTypes = selectedWord.type.toSet()
                definition = selectedWord.definition
                translation = selectedWord.getTranslationForLanguage(viewModel.getTranslationLanguage())
            }
        )
    }

    if (showDuplicateWarning) {
        AlertDialog(
            onDismissRequest = { showDuplicateWarning = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Word Already Exists") },
            text = {
                Text("The word \"$word\" is already in your collection. Please choose a different word.")
            },
            confirmButton = {
                TextButton(onClick = { showDuplicateWarning = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// Dialog for loading random words from pool
@Composable
fun LoadFromPoolDialog(
    viewModel: WordViewModel,
    onDismiss: () -> Unit,
    onWordsSelected: (List<ExampleWord>) -> Unit
) {
    var poolWords by remember { mutableStateOf<List<ExampleWord>>(emptyList()) }
    var selectedWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    val loadCount by remember { mutableIntStateOf(3) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        poolWords = viewModel.getRandomWordsFromPool(loadCount)
        println("LoadFromPoolDialog: Loaded ${poolWords.size} words")
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null)
                Text("Word Pool")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select words to add:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                selectedWords = emptySet()
                                poolWords = viewModel.getRandomWordsFromPool(loadCount)
                                println("Refresh: Loaded ${poolWords.size} words")
                                isLoading = false
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh")
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (poolWords.isEmpty()) {
                    // Empty state
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "No words available",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "No words found for your level. Try changing your level in settings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        poolWords.forEach { word ->
                            WordPoolItem(
                                word = word,
                                isSelected = selectedWords.contains(word.word),
                                onSelectChange = { isSelected ->
                                    selectedWords = if (isSelected) {
                                        selectedWords + word.word
                                    } else {
                                        selectedWords - word.word
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selected = poolWords.filter { selectedWords.contains(it.word) }
                    onWordsSelected(selected)
                },
                enabled = selectedWords.isNotEmpty()
            ) {
                Text("Add Selected")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WordPoolItem(
    word: ExampleWord,
    isSelected: Boolean,
    onSelectChange: (Boolean) -> Unit
) {
    Card(
        onClick = { onSelectChange(!isSelected) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = word.getTypeString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Dialog for searching a specific word in pool
@Composable
fun SearchInPoolDialog(
    viewModel: WordViewModel,
    onDismiss: () -> Unit,
    onWordSelected: (ExampleWord) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<ExampleWord?>(null) }
    var showNotFound by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Text("Search in Word Pool")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showNotFound = false
                        searchResult = null
                    },
                    label = { Text("Enter word") },
                    placeholder = { Text("e.g., dog") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    scope.launch {
                                        isSearching = true
                                        val result = viewModel.searchWordInPool(searchQuery.trim())
                                        searchResult = result
                                        showNotFound = result == null
                                        isSearching = false
                                    }
                                }
                            }
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                )

                if (showNotFound) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Word not found in pool",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                searchResult?.let { word ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Text(
                                text = word.getTypeString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            HorizontalDivider()

                            Text(
                                text = word.definition,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = {
                                    onWordSelected(word)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use This Word")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Reusable text field component
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(icon, contentDescription = null)
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}