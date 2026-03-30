package com.yomusensei.data.model

data class DictionaryEntry(
    val word: String,
    val reading: String,
    val meanings: List<Meaning>,
    val examples: List<Example> = emptyList(),
    val jlptLevel: String? = null,
    val commonness: Int = 0
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<String>,
    val tags: List<String> = emptyList()
)

data class Example(
    val japanese: String,
    val reading: String,
    val translation: String
)
