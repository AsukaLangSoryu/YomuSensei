package com.yomusensei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yomusensei.data.api.GeminiRepository
import com.yomusensei.data.api.SettingsRepository
import com.yomusensei.data.vocabulary.VocabularyDatabase
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.web.WebScraper
import com.yomusensei.ui.AppNavigation
import com.yomusensei.ui.home.HomeViewModel
import com.yomusensei.ui.reader.ReaderViewModel
import com.yomusensei.ui.settings.SettingsViewModel
import com.yomusensei.ui.theme.Background
import com.yomusensei.ui.theme.YomuSenseiTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var geminiRepository: GeminiRepository
    private lateinit var webScraper: WebScraper
    private lateinit var vocabularyRepository: VocabularyRepository

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var readerViewModel: ReaderViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化依赖
        settingsRepository = SettingsRepository(applicationContext)
        geminiRepository = GeminiRepository(settingsRepository)
        webScraper = WebScraper()
        val database = VocabularyDatabase.getDatabase(applicationContext)
        vocabularyRepository = VocabularyRepository(database.vocabularyDao())

        // 初始化 ViewModels
        homeViewModel = HomeViewModel(geminiRepository, webScraper)
        readerViewModel = ReaderViewModel(geminiRepository, settingsRepository, vocabularyRepository)
        settingsViewModel = SettingsViewModel(settingsRepository)

        setContent {
            YomuSenseiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    AppNavigation(
                        homeViewModel = homeViewModel,
                        readerViewModel = readerViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
