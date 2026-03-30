package com.yomusensei.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DictionaryDao {

    @Query("SELECT * FROM dictionary WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): DictionaryWord?

    @Query("SELECT * FROM dictionary WHERE word LIKE :query OR reading LIKE :query LIMIT 20")
    suspend fun searchWords(query: String): List<DictionaryWord>

    @Query("SELECT * FROM dictionary WHERE reading LIKE :pattern ORDER BY reading LIMIT 100")
    suspend fun getWordsByKana(pattern: String): List<DictionaryWord>

    @Query("SELECT * FROM dictionary ORDER BY frequency LIMIT 100")
    suspend fun getAllWordsSorted(): List<DictionaryWord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: DictionaryWord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictionaryWord>)

    @Query("SELECT COUNT(*) FROM dictionary")
    suspend fun getWordCount(): Int
}
