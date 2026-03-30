package com.yomusensei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yomusensei.data.api.JishoApiService
import com.yomusensei.data.api.MemoryRepository
import com.yomusensei.data.api.SettingsRepository
import com.yomusensei.data.api.TavilyApiService
import com.yomusensei.data.api.tools.ToolExecutor
import com.yomusensei.data.local.DictionaryInitializer
import com.yomusensei.data.local.DictionaryRepository
import com.yomusensei.data.vocabulary.VocabularyDatabase
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.web.SyosetuScraper
import com.yomusensei.data.web.WebScraper
import com.yomusensei.data.web.WikipediaScraper
import com.yomusensei.ui.AppNavigation
import com.yomusensei.ui.home.HomeViewModel
import com.yomusensei.ui.reader.ReaderViewModel
import com.yomusensei.ui.settings.SettingsViewModel
import com.yomusensei.ui.theme.Background
import com.yomusensei.ui.theme.YomuSenseiTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var memoryRepository: MemoryRepository
    private lateinit var webScraper: WebScraper
    private lateinit var vocabularyRepository: VocabularyRepository

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var readerViewModel: ReaderViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsRepository = SettingsRepository(applicationContext)
        memoryRepository = MemoryRepository(applicationContext)
        val aiProvider = runBlocking { settingsRepository.buildAiProvider() }
        webScraper = WebScraper(applicationContext)
        val syosetuScraper = SyosetuScraper()
        val wikipediaScraper = WikipediaScraper()
        val database = VocabularyDatabase.getDatabase(applicationContext)
        vocabularyRepository = VocabularyRepository(database.vocabularyDao())

        val jishoService = JishoApiService.create()
        val tavilyService = TavilyApiService.create()
        val dictionaryRepository = DictionaryRepository(database.dictionaryDao(), jishoService)

        // 初始化核心词典（后台执行）
        val dictionaryInitializer = DictionaryInitializer(applicationContext, database.dictionaryDao())
        runBlocking {
            dictionaryInitializer.initializeIfNeeded()
        }

        val toolExecutor = ToolExecutor(
            webScraper,
            vocabularyRepository,
            jishoService,
            tavilyService,
            "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",
            syosetuScraper,
            wikipediaScraper
        )
        homeViewModel = HomeViewModel(aiProvider, webScraper, toolExecutor, memoryRepository)
        readerViewModel = ReaderViewModel(aiProvider, settingsRepository, vocabularyRepository, jishoService, memoryRepository, dictionaryRepository)
        settingsViewModel = SettingsViewModel(settingsRepository, dictionaryRepository)

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
