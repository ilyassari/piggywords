package com.ellez.piggywords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.ellez.piggywords.ui.screens.*
import com.ellez.piggywords.ui.theme.PiggyWordsTheme
import com.ellez.piggywords.ui.viewmodel.WordViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiggyWordsTheme {
                PiggyWordsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiggyWordsApp(
    viewModel: WordViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val todayStudiedCount by viewModel.todayStudiedCount.collectAsState()
    val pendingWordCount by viewModel.pendingWordCount.collectAsState()

    // Hide bottom bar on detail screens
    val showBottomBar = currentDestination?.route?.startsWith("word_detail/") != true &&
            currentDestination?.route?.startsWith("edit_word/") != true &&
            currentDestination?.route != "word_list"

    Scaffold(
        topBar = {
            if (showBottomBar) {
                val context = LocalContext.current
                val mascotBitmap = remember {
                    context.assets.open("mascot.png").use {
                        BitmapFactory.decodeStream(it)
                    }
                }

                TopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = mascotBitmap.asImageBitmap(),
                                contentDescription = "Mascot",
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Fit
                            )

                            Text(
                                text = when (currentDestination?.route) {
                                    "home" -> "Piggy Words"
                                    "add_word" -> "Add New Word"
                                    "study" -> "Study Time"
                                    "settings" -> "Settings"
                                    else -> "Piggy Words"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        // Display today's study count badge
                        if (currentDestination?.route == "home" && todayStudiedCount > 0) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = Color(0xFF5FB8A7).copy(alpha = 0.3f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF5FB8A7)
                                    )
                                    Text(
                                        text = "$todayStudiedCount",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1A1C), // Top: Black
                                Color(0xFF2D5049)  // Bottom: Green
                            )
                        )
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1A1A1C),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        onClick = {
                            if (currentDestination?.route != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                "Home",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF5FB8A7),
                            selectedTextColor = Color(0xFF5FB8A7),
                            unselectedIconColor = Color(0xFF8E8E93),
                            unselectedTextColor = Color(0xFF8E8E93),
                            indicatorColor = Color(0xFF2D5049)
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "add_word" } == true,
                        onClick = {
                            if (currentDestination?.route != "add_word") {
                                navController.navigate("add_word")
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = {
                            Text(
                                "Add",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFF8E8E93),
                            unselectedTextColor = Color(0xFF8E8E93),
                            indicatorColor = Color(0xFF2D5049)
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "study" } == true,
                        onClick = {
                            if (currentDestination?.route != "study") {
                                navController.navigate("study")
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingWordCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFFF6B6B)
                                        ) {
                                            Text(
                                                text = if (pendingWordCount > 99) "99+" else "$pendingWordCount",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                "Study",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFF8E8E93),
                            unselectedTextColor = Color(0xFF8E8E93),
                            indicatorColor = Color(0xFF2D5049)
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                        onClick = {
                            if (currentDestination?.route != "settings") {
                                navController.navigate("settings")
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFF8E8E93),
                            unselectedTextColor = Color(0xFF8E8E93),
                            indicatorColor = Color(0xFF2D5049)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn() + slideIntoContainer(towards = SlideDirection.Start)
            },
            exitTransition = {
                fadeOut() + slideOutOfContainer(towards = SlideDirection.Start)
            }
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToWordList = { typeFilter, levelFilter ->
                        val route = buildString {
                            append("word_list")
                            if (typeFilter != null || levelFilter != null) {
                                append("?")
                                if (typeFilter != null) append("type=$typeFilter")
                                if (typeFilter != null && levelFilter != null) append("&")
                                if (levelFilter != null) append("level=$levelFilter")
                            }
                        }
                        navController.navigate(route)
                    },
                    onNavigateToStudy = {
                        navController.navigate("study")
                    }
                )
            }
            composable("add_word") {
                AddWordScreen(
                    viewModel = viewModel,
                    onWordAdded = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("study") {
                StudyScreen(viewModel = viewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            composable(
                route = "word_list?type={type}&level={level}",
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("level") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val typeFilter = backStackEntry.arguments?.getString("type")
                val levelFilter = backStackEntry.arguments?.getString("level")

                WordListScreen(
                    viewModel = viewModel,
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate("word_detail/$wordId")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    initialTypeFilter = typeFilter,
                    initialLevelFilter = levelFilter
                )
            }
            composable("word_detail/{wordId}") { backStackEntry ->
                val wordId = backStackEntry.arguments?.getString("wordId")?.toIntOrNull() ?: 0
                WordDetailScreen(
                    wordId = wordId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate("edit_word/$id")
                    }
                )
            }
            composable("edit_word/{wordId}") { backStackEntry ->
                val wordId = backStackEntry.arguments?.getString("wordId")?.toIntOrNull() ?: 0
                EditWordScreen(
                    wordId = wordId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onWordUpdated = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}