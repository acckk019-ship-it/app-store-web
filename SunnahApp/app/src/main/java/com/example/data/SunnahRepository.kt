package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class SunnahRepository(private val sunnahDao: SunnahDao) {

    val allSunnahs: Flow<List<Sunnah>> = sunnahDao.getAllSunnahs()
    val bookmarkedSunnahs: Flow<List<Sunnah>> = sunnahDao.getBookmarkedSunnahs()
    val practicedSunnahs: Flow<List<Sunnah>> = sunnahDao.getPracticedSunnahs()
    val totalCount: Flow<Int> = sunnahDao.getCount()
    val practicedCount: Flow<Int> = sunnahDao.getPracticedCount()

    fun getSunnahsByCategory(category: String): Flow<List<Sunnah>> {
        return sunnahDao.getSunnahsByCategory(category)
    }

    fun searchSunnahs(query: String): Flow<List<Sunnah>> {
        return sunnahDao.searchSunnahs(query)
    }

    suspend fun insertSunnah(sunnah: Sunnah) {
        sunnahDao.insertSunnah(sunnah)
    }

    suspend fun updateSunnah(sunnah: Sunnah) {
        sunnahDao.updateSunnah(sunnah)
    }

    suspend fun deleteSunnah(sunnah: Sunnah) {
        sunnahDao.deleteSunnah(sunnah)
    }

    suspend fun toggleBookmark(sunnah: Sunnah) {
        val updated = sunnah.copy(isBookmarked = !sunnah.isBookmarked)
        sunnahDao.updateSunnah(updated)
    }

    suspend fun logPractice(sunnah: Sunnah) {
        val currentlyPracticedToday = sunnah.isPracticedToday
        val updated = sunnah.copy(
            isPracticedToday = !currentlyPracticedToday,
            practicedCount = if (!currentlyPracticedToday) sunnah.practicedCount + 1 else maxOf(0, sunnah.practicedCount - 1)
        )
        sunnahDao.updateSunnah(updated)
    }

    suspend fun resetDailyPractices() {
        sunnahDao.resetDailyPractices()
    }

    /**
     * Search Gemini AI for new Sunnahs on a custom topic and save them into the local database
     */
    suspend fun searchAndImportAISunnahs(topic: String, defaultCategory: String): Result<List<Sunnah>> {
        return try {
            val results = GeminiClient.fetchSunnahsFromAI(topic)
            if (results.isNotEmpty()) {
                val adaptedResults = results.map { 
                    it.copy(category = defaultCategory) 
                }
                sunnahDao.insertAll(adaptedResults)
                Result.success(adaptedResults)
            } else {
                Result.failure(Exception("لم يتم العثور على سنن صحيحة مطابقة للموضوع"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
