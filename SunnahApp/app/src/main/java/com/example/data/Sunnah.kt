package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "sunnah_table")
data class Sunnah(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,          // Category name in Arabic (e.g., "اليوم والليلة والنوم", "الطهارة والسواك")
    val title: String,             // Short title of the Sunnah
    val arabicText: String,        // Explicit Arabic text/Hadith detailing the Sunnah
    val explanation: String,       // Simple explanation of how to practice it and its rewards
    val source: String,            // Source (e.g., "البخاري", "مسلم", "أبو داود")
    val isBookmarked: Boolean = false,
    val isPracticedToday: Boolean = false,
    val practicedCount: Int = 0,
    val isUserAdded: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
