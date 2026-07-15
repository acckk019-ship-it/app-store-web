package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.AppScreen
import com.example.ui.SunnahViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.MindmapScreen
import com.example.ui.screens.SunnahListScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: SunnahViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Full bleed content
        
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val selectedSunnah by viewModel.selectedSunnah.collectAsState()
                val appControl by viewModel.appControl.collectAsState()
                val updateAvailable by viewModel.updateAvailable.collectAsState()
                val context = LocalContext.current

                // ── Force-Close Dialog (blocks all app interaction) ──
                if (appControl.forceClose) {
                    ForceCloseDialog(
                        message = appControl.forceCloseMessage.ifBlank {
                            "تم إيقاف التطبيق مؤقتاً. الرجاء المحاولة لاحقاً."
                        }
                    )
                    return@MyApplicationTheme // Render nothing else
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        CustomBottomNavigationBar(
                            currentScreen = currentScreen,
                            onTabSelected = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        // ── Under-Development Banner ──
                        if (appControl.underDevelopment) {
                            UnderDevelopmentBanner(
                                message = appControl.underDevelopmentMessage.ifBlank {
                                    "التطبيق قيد التطوير. قد تواجه بعض المشكلات."
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            // Custom fade transitions between modules
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "screen_transit"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    AppScreen.MINDMAP -> MindmapScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    AppScreen.EXPLORER -> SunnahListScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    AppScreen.CALENDAR -> CalendarScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    AppScreen.SETTINGS -> { /* Reserved */ }
                                }
                            }

                            // Detailed Popup Dialog of the selected Sunnah
                            selectedSunnah?.let { sunnah ->
                                SunnahDetailDialog(
                                    sunnah = sunnah,
                                    onDismiss = { viewModel.selectSunnah(null) },
                                    onBookmarkToggle = { viewModel.toggleBookmark(sunnah) },
                                    onPracticeToggle = { viewModel.logPractice(sunnah) }
                                )
                            }
                        }
                    }
                }

                // ── Update Available Dialog ── (shown on top of everything, dismissible)
                if (updateAvailable) {
                    UpdateAvailableDialog(
                        newVersion = appControl.latestVersionName.ifBlank { appControl.latestVersion },
                        message = appControl.updateMessage,
                        updateUrl = appControl.updateUrl,
                        onDismiss = { /* User can dismiss; we don't force update */ },
                        onUpdate = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(appControl.updateUrl.ifBlank {
                                    "https://acckk019-ship-it.github.io/app-store-web/"
                                })
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = NavigationBackground,
        tonalElevation = 10.dp,
        modifier = Modifier
            .navigationBarsPadding() // Keep active labels clear of system gesture bars
            .height(72.dp)
            .border(0.5.dp, GoldClassic.copy(alpha = 0.2f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val tabs = listOf(
            Triple(AppScreen.DASHBOARD, "الرئيسية", Icons.Default.Dashboard),
            Triple(AppScreen.MINDMAP, "الخريطة", Icons.Default.AccountTree),
            Triple(AppScreen.EXPLORER, "السنن", Icons.Default.MenuBook),
            Triple(AppScreen.CALENDAR, "التقويم", Icons.Default.CalendarMonth)
        )

        tabs.forEach { tab ->
            val screen = tab.first
            val label = tab.second
            val icon = tab.third
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) EmeraldDark else SecondaryText,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) GoldClassic else SecondaryText
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GoldClassic,
                    selectedIconColor = EmeraldDark,
                    unselectedIconColor = SecondaryText,
                    selectedTextColor = GoldClassic,
                    unselectedTextColor = SecondaryText
                ),
                modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
            )
        }
    }
}

@Composable
fun SunnahDetailDialog(
    sunnah: com.example.data.Sunnah,
    onDismiss: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onPracticeToggle: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .wrapContentHeight()
                .border(1.5.dp, GoldClassic, RoundedCornerShape(20.dp))
                .testTag("detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Category
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

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = sunnah.title,
                    color = GoldClassic,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hadith Sacred container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "« ${sunnah.arabicText} »",
                        color = TextGreen,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Justify,
                        style = LocalTextStyle.current.copy(
                            textDirection = TextDirection.Rtl
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanation/Practice Benefits Section
                Text(
                    text = "شرح كيفية تطبيق السنة وفضلها:",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = sunnah.explanation,
                    color = IvoryWhite,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Justify,
                    style = LocalTextStyle.current.copy(
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Source References
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "المرجع",
                        tint = GoldClassic,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "التخريج والمصدر: ${sunnah.source}",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(
                                AnnotatedString("${sunnah.title}\n\n${sunnah.arabicText}\n\nالمصدر: ${sunnah.source}\nتم نسخه من تطبيق السنن النبوية")
                            )
                        },
                        border = BorderStroke(1.dp, GoldClassic),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldClassic),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Bookmark
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .border(1.dp, GoldClassic.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = if (sunnah.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "تفضيل",
                            tint = if (sunnah.isBookmarked) GoldClassic else SecondaryText
                        )
                    }

                    // Log practice
                    Button(
                        onClick = {
                            onPracticeToggle()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sunnah.isPracticedToday) GoldAccent else GoldClassic,
                            contentColor = EmeraldDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(
                            text = if (sunnah.isPracticedToday) "تراجع عن ممارسة اليوم" else "مارستها اليوم!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
// Remote App Control Dialogs
// Driven by control.json fetched from GitHub Pages on startup
// ════════════════════════════════════════════════════════════

/**
 * Full-screen blocking dialog shown when forceClose = true in control.json.
 * The user CANNOT dismiss this dialog — the app is effectively closed.
 */
@Composable
fun ForceCloseDialog(message: String) {
    Dialog(
        onDismissRequest = { /* Non-dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0D1B2A),
                            Color(0xFF1B2838)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // Lock icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                        .border(2.dp, Color(0xFFE53935).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "مغلق",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "التطبيق غير متاح",
                    color = Color(0xFFEF5350),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = TextDirection.Rtl
                    )
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "يرجى متابعة المتجر للتحديثات",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * A persistent amber warning banner displayed at the top of the screen
 * when underDevelopment = true in control.json.
 */
@Composable
fun UnderDevelopmentBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Construction,
                contentDescription = null,
                tint = Color(0xFF1C1C1E),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFF1C1C1E),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    textDirection = TextDirection.Rtl
                )
            )
        }
    }
}

/**
 * A dismissible update-available dialog shown when the remote version > current build.
 * Tapping "تحديث" opens the app store page in the browser.
 */
@Composable
fun UpdateAvailableDialog(
    newVersion: String,
    message: String,
    updateUrl: String,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    var dismissed by remember { mutableStateOf(false) }
    if (dismissed) return

    Dialog(onDismissRequest = { dismissed = true }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Update icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                        .border(2.dp, Color(0xFF4CAF50).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = "تحديث",
                        tint = Color(0xFF66BB6A),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "تحديث جديد متاح!",
                    color = Color(0xFF66BB6A),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (newVersion.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "الإصدار $newVersion",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            textDirection = TextDirection.Rtl
                        )
                    )
                    Spacer(Modifier.height(20.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dismiss button
                    OutlinedButton(
                        onClick = { dismissed = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("لاحقاً", color = Color.White.copy(alpha = 0.7f))
                    }

                    // Update button
                    Button(
                        onClick = {
                            onUpdate()
                            dismissed = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("تحديث", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
