package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SunnahDao {
    @Query("SELECT * FROM sunnah_table ORDER BY id ASC")
    fun getAllSunnahs(): Flow<List<Sunnah>>

    @Query("SELECT * FROM sunnah_table WHERE category = :category ORDER BY id ASC")
    fun getSunnahsByCategory(category: String): Flow<List<Sunnah>>

    @Query("SELECT * FROM sunnah_table WHERE isBookmarked = 1")
    fun getBookmarkedSunnahs(): Flow<List<Sunnah>>

    @Query("SELECT * FROM sunnah_table WHERE practicedCount > 0")
    fun getPracticedSunnahs(): Flow<List<Sunnah>>

    @Query("SELECT * FROM sunnah_table WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' OR explanation LIKE '%' || :query || '%'")
    fun searchSunnahs(query: String): Flow<List<Sunnah>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sunnahs: List<Sunnah>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSunnah(sunnah: Sunnah)

    @Update
    suspend fun updateSunnah(sunnah: Sunnah)

    @Delete
    suspend fun deleteSunnah(sunnah: Sunnah)

    @Query("SELECT COUNT(*) FROM sunnah_table")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sunnah_table WHERE practicedCount > 0")
    fun getPracticedCount(): Flow<Int>

    @Query("UPDATE sunnah_table SET isPracticedToday = 0")
    suspend fun resetDailyPractices()
}
