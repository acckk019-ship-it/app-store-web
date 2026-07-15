package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Sunnah
import com.example.ui.AppScreen
import com.example.ui.SunnahViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: SunnahViewModel,
    modifier: Modifier = Modifier
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val practicedCount by viewModel.practicedCount.collectAsState()
    val challengePercent by viewModel.challengePercent.collectAsState()
    val streak by viewModel.streakDays.collectAsState()
    val allSunnahs by viewModel.allSunnahs.collectAsState()
    val bookmarkedList by viewModel.bookmarkedSunnahs.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Geometric Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, GoldClassic.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(EmeraldLight, EmeraldDark)
                        )
                    )
            ) {
                // Subtle overlay geometric drawing on canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    // Draw decorative intersecting lines mimicking Islamic starry art
                    drawCircle(
                        color = GoldClassic.copy(alpha = 0.08f),
                        radius = 200.dp.toPx(),
                        center = Offset(width, height / 2)
                    )
                    drawCircle(
                        color = GoldClassic.copy(alpha = 0.05f),
                        radius = 120.dp.toPx(),
                        center = Offset(0f, height)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.testTag("bismillah_text")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تحدي الـ 2000 سنة نبوية",
                            color = IvoryWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    // Gold Medal Frame
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(GoldClassic.copy(alpha = 0.2f))
                            .border(1.dp, GoldClassic, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Challenge KPIs and Progress Cards Group
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress Circle Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(165.dp)
                        .border(0.8.dp, GoldClassic.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Stroke indicators
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = EmeraldSoft.copy(alpha = 0.4f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = GoldClassic,
                                    startAngle = -90f,
                                    sweepAngle = (challengePercent * 360f),
                                    useCenter = false,
                                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%.1f%%", challengePercent * 100),
                                    color = GoldAccent,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الإنجاز",
                                    color = SecondaryText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تم ممارسة: $practicedCount / 2000",
                            color = IvoryWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Streaks & Count Details Card
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Streak Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .border(0.8.dp, GoldClassic.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldClassic.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "قوة الالتزام",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$streak يومًا",
                                    color = IvoryWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الالتزام اليومي",
                                    color = SecondaryText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Total Loaded Library
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(77.dp)
                            .border(0.8.dp, GoldClassic.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldClassic.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryBooks,
                                    contentDescription = "السنن المحملة",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$totalCount سنة",
                                    color = IvoryWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "المكتبة الحالية",
                                    color = SecondaryText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Navigation Quick Shortcuts
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.MINDMAP) }
                    .border(0.5.dp, GoldClassic.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoldClassic.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "الخريطة الذهنية التفاعلية",
                            tint = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض خريطة السنن الذهنية",
                            color = IvoryWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "شاهد السنن منسقة شبكياً وتصفح تفرعاتها الـ 8",
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "دخول",
                        tint = GoldClassic
                    )
                }
            }
        }

        // Theme & Day Rest Configuration Card
        item {
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldClassic.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .testTag("theme_configs_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "تخصيص المظهر",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تخصيص مظهر التطبيق وإدارة الورد",
                            color = IvoryWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Theme selector chips or circle selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "سمات الألوان الفاخرة المتاحة:",
                            color = SecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Scrollable row of color options
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.ui.theme.AppColorTheme.values().forEach { theme ->
                                val isSelected = selectedTheme == theme
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) GoldClassic.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.8.dp,
                                            color = if (isSelected) GoldClassic else GoldClassic.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.selectTheme(theme) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("theme_chip_${theme.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Color Dot indicator
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(theme.primaryColor)
                                        )
                                        Text(
                                            text = theme.displayName,
                                            color = if (isSelected) GoldLight else IvoryWhite,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = GoldClassic.copy(alpha = 0.15f), thickness = 0.5.dp)

                    // Day Rest Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تسجيل يوم راحة ومراجعة ☕",
                                color = IvoryWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "يُصفّر تحديد السنن لتبدأ غداً بنشاط، مع حفظ ومواصلة سلسلة إنجازك اليومي بفضل الله.",
                                color = SecondaryText,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                viewModel.applyDayRest()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldClassic,
                                contentColor = EmeraldDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("day_rest_button")
                        ) {
                            Text(
                                text = "يوم راحة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Selected Authentic Gems
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سنن مختارة لتطبيقها اليوم",
                    color = GoldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("section_title")
                )
                Text(
                    text = "عرض الكل",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.EXPLORER) }
                        .padding(4.dp)
                )
            }
        }

        // List load
        if (allSunnahs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldClassic)
                }
            }
        } else {
            items(allSunnahs.take(4)) { sunnah ->
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

@Composable
fun SunnahCardItem(
    sunnah: Sunnah,
    onBookmarkClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onCardClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(
                1.dp,
                if (sunnah.isPracticedToday) GoldClassic else Color.Transparent,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (sunnah.isPracticedToday) CardBackground else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldClassic.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sunnah.category,
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "نسخ الحديث",
                            tint = SecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (sunnah.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "حفظ بمفضلتي",
                            tint = if (sunnah.isBookmarked) GoldClassic else SecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hadith Title
            Text(
                text = sunnah.title,
                color = IvoryWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sacred Arabic Hadith text with quotes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldDark.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "« ${sunnah.arabicText} »",
                    color = TextGreen,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Justify,
                    style = LocalTextStyle.current.copy(
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Source indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "المرجع",
                    tint = GoldClassic.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تخريج الحديث: ${sunnah.source}",
                    color = SecondaryText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: log practice
            Button(
                onClick = onPracticeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sunnah.isPracticedToday) GoldClassic else EmeraldSoft,
                    contentColor = if (sunnah.isPracticedToday) EmeraldDark else IvoryWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("practice_button_${sunnah.id}")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sunnah.isPracticedToday) Icons.Default.CheckCircle else Icons.Default.DoneOutline,
                        contentDescription = "تفعيل"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (sunnah.isPracticedToday) "تم تطبيق السنة اليوم (اضغط للتراجع)" else "مارستُ هذه السُنّة اليوم",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
