package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.NetherGold
import com.example.ui.theme.StoneSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: ChatMessage,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeString = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .testTag("chat_message_${message.id}"),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMine) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StoneSurfaceVariant)
                    .border(1.dp, MinecraftBorder, RoundedCornerShape(6.dp))
            ) {
                AsyncImage(
                    model = message.senderSkinUrl,
                    contentDescription = "${message.senderMinecraftUsername} avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Header with username & timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = if (message.isMine) "You" else message.senderUsername,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (message.isMine) MinecraftGreen else DiamondCyan
                )

                if (!message.isMine) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${message.senderMinecraftUsername})",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeString,
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.7f)
                )
            }

            // Message Bubble Card
            Surface(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (message.isMine) 12.dp else 2.dp,
                    bottomEnd = if (message.isMine) 2.dp else 12.dp
                ),
                color = if (message.isMine) MinecraftGreen.copy(alpha = 0.18f) else BedrockSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (message.isMine) MinecraftGreen.copy(alpha = 0.4f) else MinecraftBorder
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    if (message.imageUrl != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StoneSurfaceVariant)
                                .border(1.dp, MinecraftBorder, RoundedCornerShape(8.dp))
                                .clickable { onImageClick(message.imageUrl) }
                                .testTag("message_image_${message.id}")
                        ) {
                            AsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Shared Minecraft Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    onSendMessage: (text: String, imageUrl: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var showPresetPicker by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = BedrockSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MinecraftBorder)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Selected Image Preview Thumbnail
            if (selectedImageUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MinecraftGreen, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Image preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Photo attached",
                            color = MinecraftGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap send to share with the group",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                    TextButton(onClick = { selectedImageUri = null }) {
                        Text("Remove", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Photo Picker button (Gallery)
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("pick_photo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach Photo",
                        tint = DiamondCyan
                    )
                }

                // Preset Minecraft Screenshots Gallery button
                IconButton(
                    onClick = { showPresetPicker = true },
                    modifier = Modifier.testTag("preset_screenshots_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Minecraft Build Gallery",
                        tint = NetherGold
                    )
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Message #channel...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinecraftGreen,
                        unfocusedBorderColor = MinecraftBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = StoneSurfaceVariant,
                        unfocusedContainerColor = StoneSurfaceVariant
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (text.isNotBlank() || selectedImageUri != null) {
                            onSendMessage(text, selectedImageUri)
                            text = ""
                            selectedImageUri = null
                        }
                    },
                    modifier = Modifier
                        .background(MinecraftGreen, CircleShape)
                        .size(42.dp)
                        .testTag("send_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showPresetPicker) {
        PresetScreenshotsDialog(
            onSelectImage = { url ->
                selectedImageUri = url
                showPresetPicker = false
            },
            onDismiss = { showPresetPicker = false }
        )
    }
}

data class PresetScreenshot(val title: String, val url: String)

@Composable
fun PresetScreenshotsDialog(
    onSelectImage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = remember {
        listOf(
            PresetScreenshot("Epic Castle Gatehouse", "https://images.unsplash.com/photo-1627856013091-fed6e4e30025?w=800&auto=format&fit=crop&q=80"),
            PresetScreenshot("Cozy Mountain Base", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=800&auto=format&fit=crop&q=80"),
            PresetScreenshot("Netherite Fortress Raid", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80"),
            PresetScreenshot("Redstone Automation Lab", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80"),
            PresetScreenshot("Cherry Blossom Grove", "https://images.unsplash.com/photo-1522383225653-ed111181a951?w=800&auto=format&fit=crop&q=80"),
            PresetScreenshot("Ender Dragon Arena", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Minecraft Build Screenshot",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(presets) { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectImage(preset.url) },
                        colors = CardDefaults.cardColors(containerColor = BedrockSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MinecraftBorder)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            ) {
                                AsyncImage(
                                    model = preset.url,
                                    contentDescription = preset.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = preset.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(6.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MinecraftGreen)
            }
        },
        containerColor = StoneSurfaceVariant
    )
}
