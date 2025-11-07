package com.ellez.piggywords.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ellez.piggywords.ui.viewmodel.WordViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WordViewModel
) {
    val dailyLimit by viewModel.dailyWordLimit.collectAsState()
    val frequencyMultiplier by viewModel.defaultFrequencyMultiplier.collectAsState()

    var userLevel by remember { mutableStateOf(viewModel.getUserLevel()) }
    var translationLanguage by remember { mutableStateOf(viewModel.getTranslationLanguage()) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    var sliderValue by remember { mutableFloatStateOf(dailyLimit.toFloat()) }
    var showCustomInput by remember { mutableStateOf(false) }
    var customLimitText by remember { mutableStateOf("") }

    // Study screen visibility options
    var showWordTypeInQuestion by remember { mutableStateOf(viewModel.getShowWordTypeInQuestion()) }
    var showDefinitionInQuestion by remember { mutableStateOf(viewModel.getShowDefinitionInQuestion()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Customize your learning experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Level Card
            SettingCard(
                icon = Icons.Default.School,
                title = "Learning Level",
                description = "Your current English proficiency"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelOption(
                        title = "Beginner",
                        isSelected = userLevel == "beginner",
                        onClick = {
                            userLevel = "beginner"
                            viewModel.setUserLevel("beginner")
                        }
                    )
                    LevelOption(
                        title = "Intermediate",
                        isSelected = userLevel == "intermediate",
                        onClick = {
                            userLevel = "intermediate"
                            viewModel.setUserLevel("intermediate")
                        }
                    )
                    LevelOption(
                        title = "Advanced",
                        isSelected = userLevel == "advanced",
                        onClick = {
                            userLevel = "advanced"
                            viewModel.setUserLevel("advanced")
                        }
                    )
                }
            }

            // Translation Language Card
            SettingCard(
                icon = Icons.Default.Translate,
                title = "Translation Language",
                description = "Your native or preferred language"
            ) {
                ExposedDropdownMenuBox(
                    expanded = showLanguageDropdown,
                    onExpandedChange = { showLanguageDropdown = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.getLanguageDisplayName(translationLanguage),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Language") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false }
                    ) {
                        viewModel.getAllLanguages().forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(viewModel.getLanguageDisplayName(lang)) },
                                onClick = {
                                    translationLanguage = lang
                                    viewModel.setTranslationLanguage(lang)
                                    showLanguageDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Daily Word Goal Card with Slider
            SettingCard(
                icon = Icons.Default.Today,
                title = "Daily Word Goal",
                description = "Number of words to study each day"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${sliderValue.roundToInt()} words per day",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            viewModel.updateDailyWordLimit(sliderValue.roundToInt())
                        },
                        valueRange = 1f..100f,
                        steps = 98,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showCustomInput = true }
                        ) {
                            Text("Set 100+")
                        }
                        Text(
                            text = "100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Review Frequency Card with 3-step Slider
            SettingCard(
                icon = Icons.Default.Refresh,
                title = "Review Frequency",
                description = "How often words will be reviewed"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val frequencyText = when (frequencyMultiplier) {
                        2 -> "Frequent"
                        3 -> "Normal"
                        5 -> "Sparse"
                        else -> "Normal"
                    }

                    val frequencyIndex = when (frequencyMultiplier) {
                        2 -> 0f
                        3 -> 1f
                        5 -> 2f
                        else -> 1f
                    }

                    Text(
                        text = frequencyText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (frequencyMultiplier) {
                            2 -> MaterialTheme.colorScheme.error
                            3 -> MaterialTheme.colorScheme.primary
                            5 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    Slider(
                        value = frequencyIndex,
                        onValueChange = { value ->
                            val newMultiplier = when (value.roundToInt()) {
                                0 -> 2
                                1 -> 3
                                2 -> 5
                                else -> 3
                            }
                            viewModel.updateDefaultFrequencyMultiplier(newMultiplier)
                        },
                        valueRange = 0f..2f,
                        steps = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Frequent",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (frequencyMultiplier == 2)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Normal",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (frequencyMultiplier == 3)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sparse",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (frequencyMultiplier == 5)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    InfoItem(
                        icon = Icons.Default.Info,
                        text = "Frequent: Review every 2× interval"
                    )
                    InfoItem(
                        icon = Icons.Default.Info,
                        text = "Normal: Review every 3× interval"
                    )
                    InfoItem(
                        icon = Icons.Default.Info,
                        text = "Sparse: Review every 5× interval"
                    )
                }
            }

            // NEW: Study Options Card
            SettingCard(
                icon = Icons.Default.Visibility,
                title = "Study Screen Options",
                description = "Customize what you see when studying"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Word Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Word Type",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Display word type (noun, verb, etc.) on question side",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showWordTypeInQuestion,
                            onCheckedChange = {
                                showWordTypeInQuestion = it
                                viewModel.setShowWordTypeInQuestion(it)
                            }
                        )
                    }

                    HorizontalDivider()

                    // Definition Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Definition",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Display English definition on question side",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showDefinitionInQuestion,
                            onCheckedChange = {
                                showDefinitionInQuestion = it
                                viewModel.setShowDefinitionInQuestion(it)
                            }
                        )
                    }

                    HorizontalDivider()

                    InfoItem(
                        icon = Icons.Default.Info,
                        text = "On the answer side, all information is always visible"
                    )
                }
            }

            // Learning System Info Card
            SettingCard(
                icon = Icons.Default.Lightbulb,
                title = "How It Works",
                description = "Spaced repetition learning system"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoItem(
                        icon = Icons.Default.NewReleases,
                        text = "New words start at Level 0"
                    )
                    InfoItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        text = "Each correct answer increases the level"
                    )
                    InfoItem(
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        text = "Incorrect answers reset to previous level"
                    )
                    InfoItem(
                        icon = Icons.Default.Schedule,
                        text = "Review intervals grow with your progress"
                    )
                    InfoItem(
                        icon = Icons.Default.EmojiEvents,
                        text = "Level 6+ words can be marked as 'Mastered'"
                    )
                }
            }

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Piggy Words",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "The fun way to learn vocabulary",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Custom Limit Dialog
    if (showCustomInput) {
        AlertDialog(
            onDismissRequest = { showCustomInput = false },
            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
            title = { Text("Set Custom Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a number greater than 100:")
                    OutlinedTextField(
                        value = customLimitText,
                        onValueChange = {
                            customLimitText = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Number of words") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = customLimitText.toIntOrNull()
                        if (limit != null && limit > 100) {
                            sliderValue = 100f
                            viewModel.updateDailyWordLimit(limit)
                            showCustomInput = false
                            customLimitText = ""
                        }
                    },
                    enabled = customLimitText.toIntOrNull()?.let { it > 100 } == true
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomInput = false
                    customLimitText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            content()
        }
    }
}

@Composable
fun LevelOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}