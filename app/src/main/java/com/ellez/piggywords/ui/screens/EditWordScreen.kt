package com.ellez.piggywords.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ellez.piggywords.ui.viewmodel.WordViewModel
import com.ellez.piggywords.util.WordTypes
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditWordScreen(
    wordId: Int,
    viewModel: WordViewModel,
    onNavigateBack: () -> Unit,
    onWordUpdated: () -> Unit
) {
    val allWords by viewModel.allWords.collectAsState()
    val word = remember(allWords, wordId) { allWords.find { it.id == wordId } }

    // State variables for form fields
    var editedWord by remember { mutableStateOf("") }
    var selectedWordTypes by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var definition by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImagePath by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val wordTypesMap = remember { WordTypes.getAllTypes() }

    // Initialize form with existing word data
    LaunchedEffect(word) {
        word?.let {
            editedWord = it.word
            selectedWordTypes = WordTypes.stringToTypeList(it.wordType).toSet()
            definition = it.definition
            translation = it.translation
            existingImagePath = it.imagePath
        }
    }

    // Check if form has changes
    val hasChanges = word?.let {
        editedWord != it.word ||
                selectedWordTypes != WordTypes.stringToTypeList(it.wordType).toSet() ||
                definition != it.definition ||
                translation != it.translation ||
                selectedImageUri != null
    } ?: false

    val isFormValid = editedWord.isNotBlank() && selectedWordTypes.isNotEmpty() &&
            definition.isNotBlank() && translation.isNotBlank()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    if (word == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Word") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showDiscardDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
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
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Edit Word",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Update your word card",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Word Input Field
                OutlinedTextField(
                    value = editedWord,
                    onValueChange = { editedWord = it },
                    label = { Text("Word") },
                    placeholder = { Text("e.g., serendipity") },
                    leadingIcon = {
                        Icon(Icons.Default.Abc, contentDescription = null)
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
                            text = "Select one or more types",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            wordTypesMap.forEach { (typeId, typeName) ->
                                val isSelected = selectedWordTypes.contains(typeId)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedWordTypes = if (isSelected) {
                                            selectedWordTypes - typeId
                                        } else {
                                            selectedWordTypes + typeId
                                        }
                                    },
                                    label = { Text(typeName) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                }

                // Definition Field
                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = { Text("Definition") },
                    placeholder = { Text("Enter English definition") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
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

                // Translation Field
                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text("Translation") },
                    placeholder = { Text("Enter translation in your language") },
                    leadingIcon = {
                        Icon(Icons.Default.Translate, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
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

                        // Display existing or new image
                        val imageToDisplay = selectedImageUri ?: existingImagePath?.let { Uri.parse(it) }

                        if (imageToDisplay != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = imageToDisplay,
                                    contentDescription = "Word image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                // Remove image button
                                IconButton(
                                    onClick = {
                                        selectedImageUri = null
                                        existingImagePath = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (imageToDisplay != null) Icons.Default.Image else Icons.Default.AddPhotoAlternate,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (imageToDisplay != null) "Change Image" else "Add Image")
                        }
                    }
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Column {
                            Text(
                                "Note: Editing this word will not reset its learning progress.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        scope.launch {
                            val finalImagePath = selectedImageUri?.toString() ?: existingImagePath
                            val typeString = WordTypes.typeListToString(selectedWordTypes.toList())

                            val updatedWord = word.copy(
                                word = editedWord.trim(),
                                wordType = typeString,
                                definition = definition.trim(),
                                translation = translation.trim(),
                                imagePath = finalImagePath
                            )

                            viewModel.updateWord(updatedWord)
                            onWordUpdated()
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Bottom spacing for scroll
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Word?") },
            text = {
                Text("Are you sure you want to delete \"${word.word}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWord(word)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Discard Changes?") },
            text = {
                Text("You have unsaved changes. Are you sure you want to discard them?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
}