package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Sunnah
import com.example.ui.MindmapNode
import com.example.ui.NodeType
import com.example.ui.SunnahViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindmapScreen(
    viewModel: SunnahViewModel,
    modifier: Modifier = Modifier
) {
    val nodes by viewModel.mindmapNodes.collectAsState()
    val allSunnahs by viewModel.allSunnahs.collectAsState()
    val filteredSunnahs by viewModel.filteredSunnahs.collectAsState()
    
    var activeCategorySheet by remember { mutableStateOf<String?>(null) }
    var activeNodeLabel by remember { mutableStateOf<String?>(null) }
    
    val clipboardManager = LocalClipboardManager.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldLight.copy(alpha = 0.4f), EmeraldDark),
                    center = Offset.Unspecified
                )
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        
        val widthDp = maxWidth
        val heightDp = maxHeight

        // Draw connections on canvas first
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)

            // Draw line from parent node to child node
            nodes.forEach { node ->
                if (node.parentId != null) {
                    val parentNode = nodes.find { it.id == node.parentId }
                    if (parentNode != null) {
                        // Determine if coordinates should be drawn
                        val isChildVisible = node.type == NodeType.CATEGORY || node.isExpanded 
                        val isParentVisible = parentNode.type == NodeType.CENTRAL || parentNode.isExpanded
                        
                        if (isChildVisible) {
                            val startX = parentNode.x * widthPx
                            val startY = parentNode.y * heightPx
                            val endX = node.x * widthPx
                            val endY = node.y * heightPx

                            // Golden curves or straight connections
                            val path = Path().apply {
                                moveTo(startX, startY)
                                cubicTo(
                                    (startX + endX) / 2, startY,
                                    (startX + endX) / 2, endY,
                                    endX, endY
                                )
                            }
                            
                            drawPath(
                                path = path,
                                color = if (node.type == NodeType.SUBCATEGORY) GoldAccent.copy(alpha = 0.5f) else GoldClassic,
                                style = Stroke(
                                    width = if (node.type == NodeType.SUBCATEGORY) 1.5.dp.toPx() else 2.5.dp.toPx(),
                                    pathEffect = if (node.type == NodeType.SUBCATEGORY) dashEffect else null
                                )
                            )
                        }
                    }
                }
            }
        }

        // Display Header Guide
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "خريطة السنن التفاعلية",
                color = GoldClassic,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "اضغط على الأقسام الرئيسية لتفريع السنن وتصفح أحاديثها",
                color = SecondaryText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Draw node elements as interactable Composable items overlaid on coordinates
        nodes.forEach { node ->
            // Check if node should be rendered
            val isVisible = node.type == NodeType.CENTRAL || 
                            node.type == NodeType.CATEGORY || 
                            node.isExpanded

            if (isVisible) {
                // Determine placement offset
                val xOffset = (node.x * widthDp.value).dp - (if (node.type == NodeType.CENTRAL) 55.dp else 45.dp)
                val yOffset = (node.y * heightDp.value).dp - (if (node.type == NodeType.CENTRAL) 55.dp else 25.dp)

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .testTag("mindmap_node_${node.id}")
                ) {
                    when (node.type) {
                        NodeType.CENTRAL -> {
                            CentralNodeCircle(
                                label = node.label,
                                onClick = { 
                                    viewModel.resetMindmap() 
                                }
                            )
                        }
                        NodeType.CATEGORY -> {
                            CategoryNodePill(
                                label = node.label,
                                isExpanded = node.isExpanded,
                                onClick = {
                                    viewModel.toggleNodeExpansion(node.id)
                                    if (node.categoryName != null) {
                                        viewModel.setCategoryFilter(node.categoryName)
                                        activeCategorySheet = node.categoryName
                                        activeNodeLabel = node.label
                                    }
                                }
                            )
                        }
                        NodeType.SUBCATEGORY -> {
                            SubCategoryNodePill(
                                label = node.label,
                                onClick = {
                                    if (node.categoryName != null) {
                                        viewModel.setCategoryFilter(node.categoryName)
                                        activeCategorySheet = node.categoryName
                                        activeNodeLabel = node.label
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Slide-up sheet to show Sunnah lists when a node is selected/expanded
        if (activeCategorySheet != null) {
            ModalBottomSheet(
                onDismissRequest = { 
                    activeCategorySheet = null 
                },
                containerColor = EmeraldDark,
                contentColor = IvoryWhite,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سنن قسم: ${activeNodeLabel ?: ""}",
                            color = GoldClassic,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.testTag("sheet_title")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val sunnahsInBranch = filteredSunnahs
                    if (sunnahsInBranch.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم يتم تنزيل سنن مخصصة لهذا التصنيف بعد. يمكنك استخدام المساعد الذكي لتنزيل 5 سنن تطابق هذا القسم فوراً!",
                                color = SecondaryText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            modifier = Modifier.heightIn(max = 400.dp)
                        ) {
                            items(sunnahsInBranch) { sunnah ->
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
        }
    }
}

@Composable
fun CentralNodeCircle(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(GoldAccent, GoldDark)
                )
            )
            .border(2.dp, IvoryWhite, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = EmeraldDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Composable
fun CategoryNodePill(
    label: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.05f else 1f, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .width(90.dp)
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isExpanded) GoldClassic else CardBackground
            )
            .border(
                1.3.dp, 
                if (isExpanded) IvoryWhite else GoldClassic.copy(alpha = 0.5f), 
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isExpanded) EmeraldDark else IvoryWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun SubCategoryNodePill(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(44.dp)
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldSoft)
            .border(0.8.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = GoldLight,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            modifier = Modifier.padding(4.dp)
        )
    }
}
