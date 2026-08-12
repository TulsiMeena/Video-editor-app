package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StickerItem
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun StickersPanel(
    stickers: List<StickerItem>,
    selectedStickerId: String?,
    onAddSticker: (String, String) -> Unit,
    onUpdateSticker: (StickerItem) -> Unit,
    onDeleteSticker: (String) -> Unit,
    onDuplicateSticker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Emoji") }
    val categories = listOf("Emoji", "Love", "Funny", "Travel", "Food", "Birthday", "Festival", "Social", "Shapes", "Arrows")

    val stickersMap = remember {
        mapOf(
            "Emoji" to listOf("🔥", "😍", "🎉", "😎", "🚀", "✨", "❤️", "🌟", "💯", "👍", "👏", "😃"),
            "Love" to listOf("❤️", "💖", "💕", "💘", "💌", "🌹", "👰", "💍", "💋", "🎁"),
            "Funny" to listOf("😂", "🤣", "🤪", "👻", "🤡", "💩", "🥳", "🙈", "🤖", "👽"),
            "Travel" to listOf("✈️", "🚗", "🏖️", "🏔️", "🗺️", "🧳", "📸", "🏕️", "🛳️", "🌅"),
            "Food" to listOf("🍕", "🍔", "🍦", "☕", "🍰", "🍩", "🍣", "🌮", "🍿", "🥤"),
            "Birthday" to listOf("🎂", "🎈", "🎁", "🎉", "🕯️", "👑", "🍰", "🍾", "🎇", "🎊"),
            "Festival" to listOf("🪔", "🎆", "🎇", "🎨", "🎪", "🏮", "✨", "🎵", "💃", "🕺"),
            "Social" to listOf("👍", "💬", "❤️", "🔔", "📌", "🏷️", "📷", "⭐", "📢", "🎯"),
            "Shapes" to listOf("🔴", "🟦", "⭐", "🔷", "🔶", "🟡", "🔺", "🔳", "🟩", "⭕"),
            "Arrows" to listOf("➡️", "⬅️", "⬆️", "⬇️", "↗️", "↘️", "🔄", "🔁", "⚡", "🎯")
        )
    }

    val selectedSticker = stickers.find { it.id == selectedStickerId }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        if (selectedSticker != null) {
            // Sticker Controls when selected
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Sticker: ${selectedSticker.symbol}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LuminaCyan
                )
                Row {
                    IconButton(onClick = { onDuplicateSticker(selectedSticker.id) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color.White)
                    }
                    IconButton(onClick = { onDeleteSticker(selectedSticker.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            // Opacity & Scale Sliders
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Opacity:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = selectedSticker.opacity,
                    onValueChange = { onUpdateSticker(selectedSticker.copy(opacity = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurface,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier.testTag("sticker_cat_$cat")
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sticker Grid
        val itemsList = stickersMap[selectedCategory] ?: emptyList()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsList.forEach { symbol ->
                Surface(
                    onClick = { onAddSticker(selectedCategory, symbol) },
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurface,
                    modifier = Modifier.testTag("sticker_item_$symbol")
                ) {
                    Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Text(text = symbol, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}
