package com.yomusensei.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.yomusensei.data.model.Article
import com.yomusensei.data.vocabulary.VocabularyDatabase
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.ui.home.HomeScreen
import com.yomusensei.ui.home.HomeViewModel
import com.yomusensei.ui.reader.ReaderScreen
import com.yomusensei.ui.reader.ReaderViewModel
import com.yomusensei.ui.settings.SettingsScreen
import com.yomusensei.ui.settings.SettingsViewModel
import com.yomusensei.ui.vocabulary.ReviewScreen
import com.yomusensei.ui.vocabulary.VocabularyScreen
import com.yomusensei.ui.vocabulary.VocabularyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Reader : Screen("reader/{articleJson}") {
        fun createRoute(article: Article): String {
            val json = Gson().toJson(article)
            val encoded = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())
            return "reader/$encoded"
        }
    }
    object Vocabulary : Screen("vocabulary")
    object VocabularyDetail : Screen("vocabulary/{wordId}") {
        fun createRoute(wordId: Long) = "vocabulary/$wordId"
    }
    object Review : Screen("review")
}

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    readerViewModel: ReaderViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val gson = remember { Gson() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Hoist VocabularyViewModel so it is shared between vocabulary and review screens
    val vocabularyViewModel: VocabularyViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = VocabularyDatabase.getDatabase(context)
                val repository = VocabularyRepository(database.vocabularyDao())
                @Suppress("UNCHECKED_CAST")
                return VocabularyViewModel(repository) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToReader = { article ->
                    navController.navigate(Screen.Reader.createRoute(article))
                },
                onNavigateToVocabulary = { navController.navigate(Screen.Vocabulary.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Reader.route,
            arguments = listOf(navArgument("articleJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleJson = backStackEntry.arguments?.getString("articleJson") ?: ""
            val decoded = URLDecoder.decode(articleJson, StandardCharsets.UTF_8.toString())
            val article = gson.fromJson(decoded, Article::class.java)

            ReaderScreen(
                viewModel = readerViewModel,
                article = article,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Vocabulary.route) {
            VocabularyScreen(
                viewModel = vocabularyViewModel,
                onNavigateToDetail = { wordId ->
                    navController.navigate(Screen.VocabularyDetail.createRoute(wordId))
                },
                onNavigateToReview = {
                    vocabularyViewModel.startReview()
                    navController.navigate(Screen.Review.route)
                }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                viewModel = vocabularyViewModel,
                onFinish = { navController.popBackStack() }
            )
        }
    }
}
