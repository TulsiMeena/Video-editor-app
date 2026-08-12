package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ImageLayerItem
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun ImageLayersPanel(
    layers: List<ImageLayerItem>,
    onToggleVisibility: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Layers, contentDescription = null, tint = LuminaCyan)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Project Layers", style = MaterialTheme.typography.titleSmall, color = Color.White)
        }

        Spacer(modifier = Modifier.padding(vertical = 4.dp))

        val displayLayers = layers.ifEmpty {
            listOf(
                ImageLayerItem(id = "layer_bg", type = "BACKGROUND", name = "Layer 1 — Background"),
                ImageLayerItem(id = "layer_photo", type = "PHOTO", name = "Layer 2 — Photo"),
                ImageLayerItem(id = "layer_sticker", type = "STICKER", name = "Layer 3 — Stickers"),
                ImageLayerItem(id = "layer_text", type = "TEXT", name = "Layer 4 — Text")
            )
        }

        displayLayers.forEach { layer ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = LuminaSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .testTag("layer_item_${layer.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = layer.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )

                    Row {
                        IconButton(onClick = { onToggleVisibility(layer.id) }) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = if (layer.isVisible) LuminaCyan else Color.Gray
                            )
                        }
                        if (layer.type != "PHOTO" && layer.type != "BACKGROUND") {
                            IconButton(onClick = { onDeleteLayer(layer.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Layer", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
