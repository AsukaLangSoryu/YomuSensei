package com.yomusensei.data.model

data class UserMemory(
    val userProfile: UserProfile = UserProfile(),
    val learningProgress: LearningProgress = LearningProgress(),
    val preferences: UserPreferences = UserPreferences(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class UserProfile(
    val japaneseLevel: String = "N5-N4",
    val learningGoals: List<String> = listOf("提升阅读能力", "阅读日语文学作品"),
    val interests: List<String> = emptyList()
)

data class LearningProgress(
    val articlesRead: Int = 0,
    val vocabularySize: Int = 0,
    val favoriteAuthors: List<String> = emptyList(),
    val recentTopics: List<String> = emptyList()
)

data class UserPreferences(
    val preferredArticleLength: String = "short",
    val preferredDifficulty: String = "beginner",
    val avoidTopics: List<String> = emptyList()
)
