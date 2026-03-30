package com.yomusensei.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary")
data class DictionaryWord(
    @PrimaryKey val word: String,
    val reading: String,
    val meanings: String, // JSON string
    val partOfSpeech: String?,
    val jlptLevel: String?,
    val frequency: Int = 0, // 词频（1-5000）
    val isCommon: Boolean = false,
    val examples: String? = null // JSON string
)
