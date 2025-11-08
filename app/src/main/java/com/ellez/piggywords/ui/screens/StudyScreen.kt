package com.ellez.piggywords.ui.screens

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ellez.piggywords.ui.viewmodel.WordViewModel
import com.ellez.piggywords.util.WordTypes
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

@Composable
fun StudyScreen(
    viewModel: WordViewModel
) {
    val context = LocalContext.current
    val studyWords by viewModel.studyWords.collectAsState()
    val dailyLimit by viewModel.dailyWordLimit.collectAsState()
    val todayStudiedCount by viewModel.todayStudiedCount.collectAsState()

    val showWordTypeInQuestion = remember { viewModel.getShowWordTypeInQuestion() }
    val showDefinitionInQuestion = remember { viewModel.getShowDefinitionInQuestion() }

    var currentIndex by remember { mutableIntStateOf(0) }

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

    val currentWord = studyWords.getOrNull(currentIndex)
    val progress = if (dailyLimit > 0) todayStudiedCount.toFloat() / dailyLimit else 0f

    // ✅ Check if daily goal is completed
    @Suppress("ConvertTwoComparisonsToRangeCheck")
    val isDailyGoalCompleted = dailyLimit > 0 && todayStudiedCount >= dailyLimit

    LaunchedEffect(studyWords.size) {
        if (studyWords.isNotEmpty() && currentIndex >= studyWords.size) {
            currentIndex = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ✅ Show completion message when daily goal is reached
        if (isDailyGoalCompleted || currentWord == null || studyWords.isEmpty()) {
            // Empty State / Goal Completed
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isDailyGoalCompleted) "Amazing!" else "Great Job!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isDailyGoalCompleted)
                        "You've completed today's goal!"
                    else if (studyWords.isEmpty())
                        "No words to study right now"
                    else
                        "You've completed today's goal!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (isDailyGoalCompleted) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$todayStudiedCount / $dailyLimit",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(
                            text = "${currentIndex + 1} of ${studyWords.size} cards",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Card Stack
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val nextWord = studyWords.getOrNull(currentIndex + 1)
                    if (nextWord != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.7f)
                                .graphicsLayer {
                                    scaleX = 0.95f
                                    scaleY = 0.95f
                                    alpha = 0.5f
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {}
                    }

                    key(currentWord.id) {
                        SwipeableCard(
                            word = currentWord,
                            showWordType = showWordTypeInQuestion,
                            showDefinition = showDefinitionInQuestion,
                            onAnswer = { isCorrect ->
                                viewModel.answerWord(currentWord.id, isCorrect)
                                if (currentIndex < studyWords.size - 1) {
                                    currentIndex++
                                } else {
                                    currentIndex = 0
                                }
                            },
                            tts = tts,
                            ttsReady = ttsReady
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Wrong Button
                    Button(
                        onClick = {
                            viewModel.answerWord(currentWord.id, false)
                            if (currentIndex < studyWords.size - 1) {
                                currentIndex++
                            } else {
                                currentIndex = 0
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Wrong",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Wrong",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Correct Button
                    Button(
                        onClick = {
                            viewModel.answerWord(currentWord.id, true)
                            if (currentIndex < studyWords.size - 1) {
                                currentIndex++
                            } else {
                                currentIndex = 0
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Correct",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Correct",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableCard(
    word: com.ellez.piggywords.data.local.WordCard,
    showWordType: Boolean,
    showDefinition: Boolean,
    onAnswer: (Boolean) -> Unit,
    tts: TextToSpeech?,
    ttsReady: Boolean
) {
    val context = LocalContext.current
    var isFlipped by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Reset flip state when word changes
    LaunchedEffect(word.id) {
        isFlipped = false
        offsetX.snapTo(0f)
        rotation.snapTo(0f)
    }

    // Ensure flip state is reset when component is first composed
    DisposableEffect(word.id) {
        isFlipped = false
        onDispose { }
    }

    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ), label = "card rotation"
    )

    // Calculate alpha based on swipe distance (make card transparent when swiping)
    val swipeAlpha = 1f - (abs(offsetX.value) / 400f).coerceIn(0f, 0.7f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
    ) {
        // FRONT CARD
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = rotation.value
                    this.rotationY = rotationY
                    // Combine rotation visibility with swipe transparency
                    alpha = if (rotationY < 90f) swipeAlpha else 0f
                    cameraDistance = 12f * density
                }
                // Use zIndex to control layering - visible card should be on top
                .zIndex(if (rotationY < 90f) 1f else 0f),
            onClick = { isFlipped = !isFlipped },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Content
                QuestionSide(
                    word = word,
                    showWordType = showWordType,
                    showDefinition = showDefinition
                )

                // Buttons on front card (top-right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // YouGlish Button
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://youglish.com/pronounce/${word.word}/english")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "YouGlish",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // TTS Button
                    IconButton(
                        onClick = {
                            if (ttsReady) {
                                tts?.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = ttsReady
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak word",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // BACK CARD
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = rotation.value
                    this.rotationY = rotationY - 180f  // Starts at -180°, ends at 0°
                    // Combine rotation visibility with swipe transparency
                    alpha = if (rotationY >= 90f) swipeAlpha else 0f
                    cameraDistance = 12f * density
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val threshold = size.width * 0.3f
                                when {
                                    offsetX.value > threshold -> {
                                        // Wait for animation to complete before calling onAnswer
                                        offsetX.animateTo(
                                            size.width.toFloat() * 1.5f,
                                            tween(300)
                                        )
                                        onAnswer(true)
                                    }

                                    offsetX.value < -threshold -> {
                                        // Wait for animation to complete before calling onAnswer
                                        offsetX.animateTo(
                                            -size.width.toFloat() * 1.5f,
                                            tween(300)
                                        )
                                        onAnswer(false)
                                    }

                                    else -> {
                                        offsetX.animateTo(0f, tween(200))
                                        rotation.animateTo(0f, tween(200))
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            rotation.snapTo(offsetX.value * 0.02f)
                        }
                    }
                }
                // Use zIndex to control layering - visible card should be on top
                .zIndex(if (rotationY >= 90f) 1f else 0f),
            onClick = { isFlipped = !isFlipped },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Content
                AnswerSide(word = word)

                // Cambridge button (top-left)
                IconButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://dictionary.cambridge.org/dictionary/english/${word.word}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Cambridge Dictionary",
                        tint = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // YouGlish + TTS buttons (top-right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // YouGlish Button
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://youglish.com/pronounce/${word.word}/english")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "YouGlish",
                            tint = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // TTS Button
                    IconButton(
                        onClick = {
                            if (ttsReady) {
                                tts?.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = ttsReady
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak word",
                            tint = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Swipe Indicators (outside both cards)
        val swipeProgress = (abs(offsetX.value) / 300f).coerceIn(0f, 1f)

        if (offsetX.value > 50f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = swipeProgress * 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        "CORRECT",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }

        if (offsetX.value < -50f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Color(0xFFE53935).copy(alpha = swipeProgress * 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        "WRONG",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionSide(
    word: com.ellez.piggywords.data.local.WordCard,
    showWordType: Boolean,
    showDefinition: Boolean
) {
    // Convert wordType IDs to display names
    val typeIds = WordTypes.stringToTypeList(word.wordType)
    val displayType = WordTypes.getTypeNames(typeIds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, start = 32.dp, end = 32.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic font size based on word length - more aggressive for single long words
        val fontSize = when {
            word.word.length <= 8 -> 52.sp
            word.word.length <= 12 -> 44.sp
            word.word.length <= 16 -> 36.sp
            word.word.length <= 20 -> 28.sp
            word.word.length <= 24 -> 24.sp
            else -> 20.sp
        }

        Text(
            text = word.word,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = word.word.contains(" "), // Only wrap if it contains spaces (multiple words)
            modifier = Modifier.fillMaxWidth()
        )

        if (showWordType) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = displayType,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        if (showDefinition) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index < word.learningLevel) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < word.learningLevel)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
fun AnswerSide(
    word: com.ellez.piggywords.data.local.WordCard
) {
    // ✅ Fixed colors for green background - always dark text for readability
    val textColor = androidx.compose.ui.graphics.Color(0xFF1B5E20) // Dark green text
    val secondaryTextColor = androidx.compose.ui.graphics.Color(0xFF2E7D32) // Medium dark green

    // Convert wordType IDs to display names
    val typeIds = WordTypes.stringToTypeList(word.wordType)
    val displayType = WordTypes.getTypeNames(typeIds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            ),
            // ✅ Dark text on green background
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = androidx.compose.ui.graphics.Color(0xFF1B5E20).copy(alpha = 0.2f)
        ) {
            Text(
                text = displayType,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = textColor.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Translation",
                style = MaterialTheme.typography.labelLarge,
                color = secondaryTextColor
            )
            Text(
                text = word.translation,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                // ✅ Dark text for better contrast
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = textColor.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Definition",
                style = MaterialTheme.typography.labelLarge,
                color = secondaryTextColor
            )
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyLarge,
                // ✅ Dark text for readability
                color = textColor.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Level ${word.learningLevel}",
                style = MaterialTheme.typography.labelMedium,
                color = secondaryTextColor
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index < word.learningLevel) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < word.learningLevel)
                                    textColor
                                else
                                    textColor.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}