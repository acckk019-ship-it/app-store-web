package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.updateAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Screen Route representations
 */
enum class AppScreen {
    DASHBOARD,
    MINDMAP,
    EXPLORER,
    CALENDAR,
    SETTINGS
}

/**
 * Represents a node in our interactive Sunnah Mindmap
 */
data class MindmapNode(
    val id: Int,
    val label: String,
    val x: Float,          // relative X coordinate (0.0 to 1.0)
    val y: Float,          // relative Y coordinate (0.0 to 1.0)
    val type: NodeType,    // Central, Category, or SubCategory
    val categoryName: String? = null, // Matches the Sunnah Entity category field
    val parentId: Int? = null,
    val isExpanded: Boolean = false,
    val description: String = ""
)

enum class NodeType {
    CENTRAL,
    CATEGORY,
    SUBCATEGORY
}

class SunnahViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SunnahRepository
    
    // Theme Customization state
    private val _selectedTheme = MutableStateFlow(AppColorTheme.OLD_EMERALD)
    val selectedTheme: StateFlow<AppColorTheme> = _selectedTheme.asStateFlow()
    
    // UI Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Database Streams
    val allSunnahs: StateFlow<List<Sunnah>>
    val bookmarkedSunnahs: StateFlow<List<Sunnah>>
    val practicedSunnahs: StateFlow<List<Sunnah>>
    val totalCount: StateFlow<Int>
    val practicedCount: StateFlow<Int>

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Filtered lists shown in the UI
    val filteredSunnahs: StateFlow<List<Sunnah>>

    // Detail sheet states
    private val _selectedSunnah = MutableStateFlow<Sunnah?>(null)
    val selectedSunnah: StateFlow<Sunnah?> = _selectedSunnah.asStateFlow()

    // Gemini AI companion state
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiResultList = MutableStateFlow<List<Sunnah>>(emptyList())
    val aiResultList: StateFlow<List<Sunnah>> = _aiResultList.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("أهلاً بك في فضاء السنن النبوية المطهرة. أنا مساعدك الذكي، يمكنك سؤالي عن صحة أي سنة، أو طلب تخريج حديث، أو كتابة موضوع (مثل: صلاة الجماعة، السواك، الأخلاق) لأبحث لك عن سنن صحيحة مأثورة وأضيفها لتحدي الـ 2000 سنة الخاص بك!" to false)
    )
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    // Streak and target challenge metrics
    val challengePercent: StateFlow<Float>

    val streakDays = MutableStateFlow(5) // Seeded base streak

    // Mindmap Node States
    private val _mindmapNodes = MutableStateFlow<List<MindmapNode>>(emptyList())
    val mindmapNodes: StateFlow<List<MindmapNode>> = _mindmapNodes.asStateFlow()

    // ── Remote App Control (from control.json on GitHub Pages) ──
    private val _appControl = MutableStateFlow(AppControl())
    val appControl: StateFlow<AppControl> = _appControl.asStateFlow()

    /** True when control.json reports a version newer than the current build */
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable.asStateFlow()

    init {
        val appDatabase = AppDatabase.getDatabase(application, viewModelScope)
        repository = SunnahRepository(appDatabase.sunnahDao())

        allSunnahs = repository.allSunnahs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        bookmarkedSunnahs = repository.bookmarkedSunnahs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        practicedSunnahs = repository.practicedSunnahs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        totalCount = repository.totalCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        practicedCount = repository.practicedCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )

        challengePercent = combine(practicedCount, totalCount) { practiced, total ->
            if (total == 0) 0f else (practiced.toFloat() / 2000f).coerceIn(0f, 1f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

        // Reactive sorting/filtering of list
        filteredSunnahs = combine(allSunnahs, _searchQuery, _selectedCategory) { list, query, cat ->
            var result = list
            if (cat != null) {
                result = result.filter { it.category == cat }
            }
            if (query.isNotEmpty()) {
                result = result.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.arabicText.contains(query, ignoreCase = true) ||
                    it.explanation.contains(query, ignoreCase = true) ||
                    it.source.contains(query, ignoreCase = true)
                }
            }
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Setup the initial interactive mindmap hierarchy
        resetMindmap()

        // Initialize dynamic color settings with old emerald default
        updateAppTheme(AppColorTheme.OLD_EMERALD)

        // Fetch remote control.json once on startup (network-safe, non-blocking)
        fetchAppControl()
    }

    /**
     * Fetches control.json from GitHub Pages and updates appControl + updateAvailable states.
     * Runs in the background; any failure leaves defaults (app behaves normally).
     */
    fun fetchAppControl() {
        viewModelScope.launch {
            val control = AppControlService.fetchControl()
            _appControl.value = control
            _updateAvailable.value = AppControlService.isUpdateAvailable(control)
        }
    }

    /**
     * Initializes or Resets mindmap graph coordinate nodes
     */
    fun resetMindmap() {
        val nodes = listOf(
            // Central main tree root
            MindmapNode(0, "السنن النبوية", 0.5f, 0.45f, NodeType.CENTRAL, description = "مُجمل هدي المصطفى صلى الله عليه وسلم"),
            
            // Major Category level nodes
            MindmapNode(1, "الطهارة والسواك", 0.20f, 0.22f, NodeType.CATEGORY, "الطهارة والسواك", parentId = 0),
            MindmapNode(2, "اليوم والليلة والنوم", 0.80f, 0.22f, NodeType.CATEGORY, "اليوم والليلة والنوم", parentId = 0),
            MindmapNode(3, "الصلاة والمساجد", 0.16f, 0.50f, NodeType.CATEGORY, "الصلاة والمساجد", parentId = 0),
            MindmapNode(4, "الطعام والشراب", 0.84f, 0.50f, NodeType.CATEGORY, "الطعام والشراب", parentId = 0),
            MindmapNode(5, "الأذكار والأدعية", 0.50f, 0.15f, NodeType.CATEGORY, "الأذكار والأدعية", parentId = 0),
            MindmapNode(6, "الأخلاق والمعاملات", 0.50f, 0.80f, NodeType.CATEGORY, "الأخلاق والمعاملات", parentId = 0),
            MindmapNode(7, "الصيام والصدقة", 0.22f, 0.76f, NodeType.CATEGORY, "الصيام والصدقة", parentId = 0),
            MindmapNode(8, "العلم والقول الطيب", 0.78f, 0.76f, NodeType.CATEGORY, "العلم والقول الطيب", parentId = 0),

            // Sub-category nodes triggered when a category expands. They are initially hidden (drawn reactively)
            MindmapNode(101, "آداب السواك", 0.12f, 0.12f, NodeType.SUBCATEGORY, "الطهارة والسواك", parentId = 1, isExpanded = false),
            MindmapNode(102, "مكملات الكفاية", 0.10f, 0.32f, NodeType.SUBCATEGORY, "الطهارة والسواك", parentId = 1, isExpanded = false),
            
            MindmapNode(201, "أذكار النوم", 0.88f, 0.12f, NodeType.SUBCATEGORY, "اليوم والليلة والنوم", parentId = 2, isExpanded = false),
            MindmapNode(202, "الاستيقاظ والتبكير", 0.90f, 0.32f, NodeType.SUBCATEGORY, "اليوم والليلة والنوم", parentId = 2, isExpanded = false),
            
            MindmapNode(301, "سمو المساجد", 0.05f, 0.45f, NodeType.SUBCATEGORY, "الصلاة والمساجد", parentId = 3, isExpanded = false),
            MindmapNode(302, "السنن الرواتب", 0.05f, 0.58f, NodeType.SUBCATEGORY, "الصلاة والمساجد", parentId = 3, isExpanded = false),
            
            MindmapNode(401, "آداب المائدة", 0.95f, 0.45f, NodeType.SUBCATEGORY, "الطعام والشراب", parentId = 4, isExpanded = false),
            MindmapNode(402, "بركة الشراب", 0.95f, 0.58f, NodeType.SUBCATEGORY, "الطعام والشراب", parentId = 4, isExpanded = false),
            
            MindmapNode(501, "سيد الاستغفار", 0.35f, 0.08f, NodeType.SUBCATEGORY, "الأذكار والأدعية", parentId = 5, isExpanded = false),
            MindmapNode(502, "حصن الخلاء", 0.65f, 0.08f, NodeType.SUBCATEGORY, "الأذكار والأدعية", parentId = 5, isExpanded = false),
            
            MindmapNode(601, "مكارم الأخلاق", 0.35f, 0.90f, NodeType.SUBCATEGORY, "الأخلاق والمعاملات", parentId = 6, isExpanded = false),
            MindmapNode(602, "الابتسامة الصادقة", 0.65f, 0.90f, NodeType.SUBCATEGORY, "الأخلاق والمعاملات", parentId = 6, isExpanded = false),
            
            MindmapNode(701, "صيام التطوع", 0.10f, 0.88f, NodeType.SUBCATEGORY, "الصيام والصدقة", parentId = 7, isExpanded = false),
            MindmapNode(702, "بركات الصدقة", 0.25f, 0.92f, NodeType.SUBCATEGORY, "الصيام والصدقة", parentId = 7, isExpanded = false),
            
            MindmapNode(801, "قول الخير والقول الطيب", 0.90f, 0.88f, NodeType.SUBCATEGORY, "العلم والقول الطيب", parentId = 8, isExpanded = false),
            MindmapNode(802, "تشجيع العلوم", 0.75f, 0.92f, NodeType.SUBCATEGORY, "العلم والقول الطيب", parentId = 8, isExpanded = false)
        )
        _mindmapNodes.value = nodes
    }

    /**
     * Toggles expansion of Category level nodes to draw SubCategory lines in Mindmap screen
     */
    fun toggleNodeExpansion(nodeId: Int) {
        _mindmapNodes.value = _mindmapNodes.value.map { node ->
            if (node.id == nodeId) {
                node.copy(isExpanded = !node.isExpanded)
            } else if (node.parentId == nodeId && node.type == NodeType.SUBCATEGORY) {
                // Also toggle visibility status on child nodes of this parent
                node.copy(isExpanded = !node.isExpanded)
            } else {
                node.copy()
            }
        }
    }

    // State Navigation handlers
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSunnah(sunnah: Sunnah?) {
        _selectedSunnah.value = sunnah
    }

    // Database Actions
    fun toggleBookmark(sunnah: Sunnah) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(sunnah)
            // Synchronize selected detail sheet if open
            if (_selectedSunnah.value?.id == sunnah.id) {
                _selectedSunnah.value = _selectedSunnah.value?.copy(isBookmarked = !sunnah.isBookmarked)
            }
        }
    }

    fun logPractice(sunnah: Sunnah) {
        viewModelScope.launch(Dispatchers.IO) {
            val originallyPracticed = sunnah.isPracticedToday
            repository.logPractice(sunnah)
            // Increment streaks for the day if a new practice was completed
            if (!originallyPracticed) {
                streakDays.value += 1
            } else {
                streakDays.value = maxOf(0, streakDays.value - 1)
            }
            
            // Synchronize Detail Sheet
            if (_selectedSunnah.value?.id == sunnah.id) {
                val updatedCount = if (!originallyPracticed) sunnah.practicedCount + 1 else maxOf(0, sunnah.practicedCount - 1)
                _selectedSunnah.value = _selectedSunnah.value?.copy(
                    isPracticedToday = !originallyPracticed,
                    practicedCount = updatedCount
                )
            }
        }
    }

    fun addCustomSunnah(title: String, arabicText: String, explanation: String, source: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sunnah = Sunnah(
                category = category,
                title = title,
                arabicText = arabicText,
                explanation = explanation,
                source = source,
                isUserAdded = true
            )
            repository.insertSunnah(sunnah)
        }
    }

    fun deleteSunnah(sunnah: Sunnah) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSunnah(sunnah)
            if (_selectedSunnah.value?.id == sunnah.id) {
                _selectedSunnah.value = null
            }
        }
    }

    // Gemini AI Operations
    fun queryAISunnahSearch(topic: String, defaultCategory: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            _chatMessages.value = _chatMessages.value + ("أرغب في الحصول على سنن نبوية عن موضوع: $topic" to true)
            
            // Invoke repository to fetch matching Sunnahs from Gemini and insert into Room
            val result = repository.searchAndImportAISunnahs(topic, defaultCategory)
            if (result.isSuccess) {
                val downloadedList = result.getOrDefault(emptyList())
                _aiResultList.value = downloadedList
                
                val message = if (downloadedList.isNotEmpty()) {
                    "وجدت لك بفضل الله ${downloadedList.size} من السنن الصحيحة والموثقة في مصادر الحديث عن '$topic'. لقد قمت بإضافتها وتثبيتها تلقائياً ضمن مكتبة السنن الخاصة بك لترتقي بتحدي الـ 2000 سنة! \n\n" +
                    downloadedList.joinToString("\n\n") { "📌 *${it.title}* \n📖 الحديث: ${it.arabicText} \n📚 المصدر: ${it.source}" }
                } else {
                    "أرجعت عملية البحث نتائج قيمة مدمجة بمكتبتك الحالية لتعظيم النفع والبركة."
                }
                _chatMessages.value = _chatMessages.value + (message to false)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "حدث خطأ غير متوقع"
                _chatMessages.value = _chatMessages.value + ("عذراً، لم نتمكن من الاتصال بالخادم الآن لتوسيع المكتبة. تم استدعاء السنن المحلية المتطابقة لتأمين استخدام كامل بدون انقطاع!\n(التفاصيل اللطيفة: الابتسامة وبشاشة الوجه متاح دائمًا!)" to false)
            }
            _aiLoading.value = false
        }
    }

    /**
     * Handle general conversational Q&A prompts with Gemini Chat companion
     */
    fun askAiCompanion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            _chatMessages.value = _chatMessages.value + (question to true)
            
            val response = GeminiClient.askGemini(question)
            _chatMessages.value = _chatMessages.value + (response to false)
            _aiLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf("أهلاً بك مجدداً. بمَ يمكنني خدمتك اليوم في سيرة وسنن الحبيب المصطفى؟" to false)
    }

    /**
     * Dynamically switches the application's color theme
     */
    fun selectTheme(theme: AppColorTheme) {
        _selectedTheme.value = theme
        updateAppTheme(theme)
    }

    /**
     * Registers a Day of Rest / Day Reset: preserving (incrementing) the habit streak 
     * while resetting the daily checkboxes so the user can start a fresh cycle
     */
    fun applyDayRest() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStreak = streakDays.value
            repository.resetDailyPractices()
            // Increment/Preserve the streak for the grace study rest day
            streakDays.value = currentStreak + 1
            
            // Sync with detail dialog if open
            if (_selectedSunnah.value != null) {
                _selectedSunnah.value = _selectedSunnah.value?.copy(isPracticedToday = false)
            }
        }
    }
}
