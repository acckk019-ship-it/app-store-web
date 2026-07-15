package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SunnahViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunnahListScreen(
    viewModel: SunnahViewModel,
    modifier: Modifier = Modifier
) {
    val filteredList by viewModel.filteredSunnahs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val bookmarkedList by viewModel.bookmarkedSunnahs.collectAsState()

    var showOnlyBookmarked by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    // Set of static categories to slider filter
    val categories = listOf(
        "الطهارة والسواك",
        "اليوم والليلة والنوم",
        "الصلاة والمساجد",
        "الطعام والشراب",
        "الأذكار والأدعية",
        "الأخلاق والمعاملات",
        "الصيام والصدقة",
        "العلم والقول الطيب"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper search panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ابحث في متون وتخريج السنن...", color = SecondaryText, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = GoldClassic) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "مسح", tint = SecondaryText)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldClassic,
                    unfocusedBorderColor = EmeraldSoft,
                    focusedTextColor = IvoryWhite,
                    unfocusedTextColor = IvoryWhite,
                    focusedContainerColor = CardBackground.copy(alpha = 0.5f),
                    unfocusedContainerColor = CardBackground.copy(alpha = 0.5f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Bookmarked Filters line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmarked toggle
                FilterChip(
                    selected = showOnlyBookmarked,
                    onClick = { showOnlyBookmarked = !showOnlyBookmarked },
                    label = { Text("المفضلة فقط ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldClassic,
                        selectedLabelColor = EmeraldDark,
                        containerColor = CardBackground,
                        labelColor = TextGreen
                    ),
                    modifier = Modifier.testTag("bookmark_toggle_chip")
                )

                if (selectedCategory != null || showOnlyBookmarked || searchQuery.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.setCategoryFilter(null)
                            showOnlyBookmarked = false
                        }
                    ) {
                        Text("إعادة ضبط الفلاتر", color = GoldAccent, fontSize = 11.sp)
                    }
                }
            }
        }

        // Categories slider row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("الكل", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldClassic,
                        selectedLabelColor = EmeraldDark,
                        containerColor = CardBackground,
                        labelColor = TextGreen
                    )
                )
            }

            items(categories) { categoryName ->
                FilterChip(
                    selected = selectedCategory == categoryName,
                    onClick = { viewModel.setCategoryFilter(categoryName) },
                    label = { Text(categoryName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldClassic,
                        selectedLabelColor = EmeraldDark,
                        containerColor = CardBackground,
                        labelColor = TextGreen
                    )
                )
            }
        }

        // Main listings
        val masterList = if (showOnlyBookmarked) {
            filteredList.filter { it.isBookmarked }
        } else {
            filteredList
        }

        if (masterList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "لا نتائج",
                        tint = GoldClassic.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (showOnlyBookmarked) "لم تقم بحفظ أي سُنّة في المفضلة بعد." else "لا توجد نتائج مطابقة لبحثك.",
                        color = SecondaryText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جرب تخفيف شروط البحث، أو تفضل بزيارة المساعد الذكي لاستكشاف سنن جديدة وحفظها!",
                        color = SecondaryText.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(masterList) { sunnah ->
                    SunnahCardItem(
                        sunnah = sunnah,
                        onBookmarkClick = { viewModel.toggleBookmark(sunnah) },
                        onPracticeClick = { viewModel.logPractice(sunnah) },
                        onCardClick = { viewModel.selectSunnah(sunnah) },
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString("${sunnah.title}\n\n${sunnah.arabicText}\n\nالمصدر: ${sunnah.source}"))
                        }
                    )
                }
            }
        }
    }
}
