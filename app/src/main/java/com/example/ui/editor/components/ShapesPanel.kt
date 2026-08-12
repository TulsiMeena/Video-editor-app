package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.ShapeItem
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun ShapesPanel(
    shapes: List<ShapeItem>,
    selectedShapeId: String?,
    onAddShape: (String) -> Unit,
    onUpdateShape: (ShapeItem) -> Unit,
    onDeleteShape: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shapeTypes = listOf("RECTANGLE", "CIRCLE", "LINE", "ARROW", "ROUNDED_RECTANGLE")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            shapeTypes.forEach { type ->
                Surface(
                    onClick = { onAddShape(type) },
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurface,
                    modifier = Modifier.testTag("shape_type_$type")
                ) {
                    Text(
                        text = type.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
