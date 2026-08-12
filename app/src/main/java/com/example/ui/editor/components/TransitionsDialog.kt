package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.ClipTransition
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun TransitionsDialog(
    currentTransition: ClipTransition?,
    maxDurationMs: Long,
    onSaveTransition: (ClipTransition) -> Unit,
    onDismiss: () -> Unit
) {
    val types = remember {
        listOf(
            "NONE", "FADE", "DISSOLVE", "BLACK", "WHITE",
            "SLIDE_LEFT", "SLIDE_RIGHT", "SLIDE_UP", "SLIDE_DOWN",
            "ZOOM", "PUSH", "WIPE"
        )
    }

    val durations = remember {
        listOf(
            100L to "0.1s",
            250L to "0.25s",
            500L to "0.5s",
            750L to "0.75s",
            1000L to "1.0s",
            2000L to "2.0s"
        ).filter { it.first <= maxDurationMs }
    }

    var selectedType by remember { mutableStateOf(currentTransition?.type ?: "NONE") }
    var selectedDurationMs by remember { mutableStateOf(currentTransition?.durationMs ?: 500L) }
    var audioCrossfade by remember { mutableStateOf(currentTransition?.audioCrossfade ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Clip Transition", color = Color.White) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Transition Style:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            onClick = { selectedType = type },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LuminaCyan else LuminaSurfaceElevated,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                            modifier = Modifier.testTag("transition_type_$type")
                        ) {
                            Text(
                                text = type.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (selectedType != "NONE") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Duration:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durations.forEach { (durMs, durLabel) ->
                            val isDurSelected = selectedDurationMs == durMs
                            Surface(
                                onClick = { selectedDurationMs = durMs },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDurSelected) LuminaCyan else LuminaSurfaceElevated,
                                border = if (isDurSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                                modifier = Modifier.testTag("transition_duration_$durLabel")
                            ) {
                                Text(
                                    text = durLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isDurSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isDurSelected) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audio Crossfade", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Switch(
                            checked = audioCrossfade,
                            onCheckedChange = { audioCrossfade = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuminaCyan),
                            modifier = Modifier.testTag("audio_crossfade_switch")
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val transition = if (selectedType == "NONE") {
                        ClipTransition(type = "NONE", durationMs = 0L)
                    } else {
                        ClipTransition(type = selectedType, durationMs = selectedDurationMs, audioCrossfade = audioCrossfade)
                    }
                    onSaveTransition(transition)
                },
                modifier = Modifier.testTag("save_transition_button")
            ) {
                Text("Apply", color = LuminaCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = LuminaSurfaceElevated
    )
}
