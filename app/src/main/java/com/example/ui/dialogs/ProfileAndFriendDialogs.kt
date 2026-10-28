package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.UserAccount
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.NetherGold
import com.example.ui.theme.StoneSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFriendDialog(
    onSendRequest: (minecraftUsername: String) -> Unit,
    onDismiss: () -> Unit
) {
    var mcUsername by remember { mutableStateOf("") }
    val popularNames = listOf("Technoblade", "Dream", "Grian", "DanTDM", "TommyInnit")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MinecraftGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Minecraft Friend",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_friend_dialog")
            ) {
                Text(
                    text = "Enter your friend's Minecraft Java or Bedrock gamertag:",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = mcUsername,
                    onValueChange = { mcUsername = it },
                    placeholder = { Text("Minecraft Username", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinecraftGreen,
                        unfocusedBorderColor = MinecraftBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Live Minecraft Skin Preview
                if (mcUsername.trim().isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BedrockSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, MinecraftBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StoneSurfaceVariant)
                        ) {
                            AsyncImage(
                                model = "https://mc-heads.net/avatar/${mcUsername.trim()}/128",
                                contentDescription = "Live skin preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = mcUsername.trim(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Live Minecraft Skin Found",
                                color = MinecraftGreen,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Quick Suggestion:",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularNames.forEach { name ->
                        Surface(
                            modifier = Modifier.clickable { mcUsername = name },
                            color = BedrockSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MinecraftBorder)
                        ) {
                            Text(
                                text = name,
                                color = DiamondCyan,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mcUsername.isNotBlank()) {
                        onSendRequest(mcUsername.trim())
                    }
                },
                enabled = mcUsername.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MinecraftGreen),
                modifier = Modifier.testTag("send_friend_request_btn")
            ) {
                Text("Send Request", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.LightGray)
            }
        },
        containerColor = StoneSurfaceVariant
    )
}

@Composable
fun EditProfileDialog(
    currentUser: UserAccount,
    onSaveProfile: (username: String, bio: String, mcUsername: String, statusText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf(currentUser.username) }
    var bio by remember { mutableStateOf(currentUser.statusText) }
    var statusText by remember { mutableStateOf(currentUser.statusText) }
    var mcUsername by remember { mutableStateOf(currentUser.minecraftUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile & Minecraft Account",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_dialog")
            ) {
                // Live Avatar & Skin
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BedrockSurface)
                            .border(2.dp, MinecraftGreen, RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = "https://mc-heads.net/avatar/${mcUsername.ifBlank { "Steve" }}/128",
                            contentDescription = "Skin preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Skin Avatar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Updates automatically with your Minecraft username",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Display Name", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_display_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinecraftGreen,
                        unfocusedBorderColor = MinecraftBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mcUsername,
                    onValueChange = { mcUsername = it },
                    label = { Text("Minecraft Gamertag / Username", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_mc_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinecraftGreen,
                        unfocusedBorderColor = MinecraftBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio", color = Color.Gray) },
                    placeholder = { Text("Tell players what you like to do in Minecraft...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_bio_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinecraftGreen,
                        unfocusedBorderColor = MinecraftBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && mcUsername.isNotBlank()) {
                        onSaveProfile(username.trim(), bio.trim(), mcUsername.trim(), statusText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MinecraftGreen),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.LightGray)
            }
        },
        containerColor = StoneSurfaceVariant
    )
}
