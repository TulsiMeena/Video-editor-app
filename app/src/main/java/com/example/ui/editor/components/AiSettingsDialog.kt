package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.AiPreferencesRepository
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import kotlinx.coroutines.launch

@Composable
fun AiSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val aiRepo = remember { AiPreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf(aiRepo.getStoredApiKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResultStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("ai_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI API Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AI features are optional. Core editing works 100% offline without account or internet. Enter an optional Gemini API Key below to unlock online AI auto captions and smart tools.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // API Key Field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        testResultStatus = null
                    },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Key Visibility",
                                tint = Color.Gray
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Row: Save | Test Connection | Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            aiRepo.setApiKey(apiKeyInput)
                            testResultStatus = Pair(true, "API Key saved securely!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_api_key_button")
                    ) {
                        Text("Save Key", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            isTesting = true
                            testResultStatus = null
                            scope.launch {
                                val res = aiRepo.testConnection(apiKeyInput)
                                isTesting = false
                                res.fold(
                                    onSuccess = {
                                        aiRepo.setApiKey(apiKeyInput)
                                        testResultStatus = Pair(true, "Connection Successful! AI Service Ready.")
                                    },
                                    onFailure = { err ->
                                        testResultStatus = Pair(false, err.message ?: "Connection failed")
                                    }
                                )
                            }
                        },
                        enabled = !isTesting && apiKeyInput.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminaCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_api_key_button")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LuminaCyan)
                        } else {
                            Text("Test", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    TextButton(
                        onClick = {
                            apiKeyInput = ""
                            aiRepo.clearApiKey()
                            testResultStatus = Pair(true, "API Key cleared.")
                        },
                        modifier = Modifier.testTag("clear_api_key_button")
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall, color = Color.Red.copy(alpha = 0.8f))
                    }
                }

                // Test Status Feedback
                testResultStatus?.let { (success, message) ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (success) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}
