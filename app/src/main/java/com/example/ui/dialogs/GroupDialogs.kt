package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Friend
import com.example.data.model.GroupChat
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.NetherGold
import com.example.ui.theme.StoneSurfaceVariant

@Composable
fun CreateGroupDialog(
    friends: List<Friend>,
    onCreateGroup: (name: String, description: String, iconEmoji: String, friendUsernames: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⚔️") }
    val selectedFriends = remember { mutableStateListOf<String>() }

    val emojis = listOf("⚔️", "🌲", "⚡", "💎", "🐉", "🏰", "🛡️", "⛏️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Minecraft Group Chat",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_group_dialog")
            ) {
                // Emoji Icon Selector
                Text(
                    text = "Group Icon",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedEmoji == emoji) MinecraftGreen.copy(alpha = 0.25f) else BedrockSurface)
                                .border(
                                    1.dp,
                                    if (selectedEmoji == emoji) MinecraftGreen else MinecraftBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name", color = Color.Gray) },
                    placeholder = { Text("e.g. Nether Raid Squad", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input"),
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Topic or Description", color = Color.Gray) },
                    placeholder = { Text("e.g. VC tactics, build showcases", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinecraftGreen,
                        unfocusedBorderColor = MinecraftBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Invite Friends (${selectedFriends.size} selected)",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (friends.isEmpty()) {
                    Text(
                        text = "No friends added yet. You can invite friends later!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        items(friends) { friend ->
                            val isChecked = selectedFriends.contains(friend.minecraftUsername)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) {
                                            selectedFriends.remove(friend.minecraftUsername)
                                        } else {
                                            selectedFriends.add(friend.minecraftUsername)
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedFriends.add(friend.minecraftUsername)
                                        else selectedFriends.remove(friend.minecraftUsername)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MinecraftGreen,
                                        uncheckedColor = MinecraftBorder
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BedrockSurface)
                                ) {
                                    AsyncImage(
                                        model = friend.skinUrl,
                                        contentDescription = friend.username,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = friend.username,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${friend.minecraftUsername})",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onCreateGroup(groupName.trim(), description.trim(), selectedEmoji, selectedFriends.toList())
                    }
                },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MinecraftGreen),
                modifier = Modifier.testTag("submit_create_group_btn")
            ) {
                Text("Create Group", color = Color.Black, fontWeight = FontWeight.Bold)
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
fun GroupParticipantsDialog(
    group: GroupChat,
    allFriends: List<Friend>,
    onInviteFriend: (friendMinecraftUsername: String) -> Unit,
    onDismiss: () -> Unit
) {
    val participantMcNames = remember(group.participants) {
        group.participants.map { it.minecraftUsername.lowercase() }.toSet()
    }
    val unjoinedFriends = remember(allFriends, participantMcNames) {
        allFriends.filter { !participantMcNames.contains(it.minecraftUsername.lowercase()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = group.iconEmoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = group.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${group.participants.size} Participants",
                        color = DiamondCyan,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                Text(
                    text = "CURRENT PARTICIPANTS",
                    color = MinecraftGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(group.participants) { participant ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BedrockSurface)
                                    .border(1.dp, MinecraftBorder, RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = participant.skinUrl,
                                    contentDescription = participant.username,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = participant.username,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "MC: ${participant.minecraftUsername}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                color = if (participant.role == "Owner") NetherGold.copy(alpha = 0.2f) else StoneSurfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (participant.role == "Owner") NetherGold else MinecraftBorder)
                            ) {
                                Text(
                                    text = participant.role,
                                    color = if (participant.role == "Owner") NetherGold else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Invite Friends section
                Text(
                    text = "INVITE FRIENDS TO GROUP",
                    color = DiamondCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (unjoinedFriends.isEmpty()) {
                    Text(
                        text = "All your friends are already in this group!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        items(unjoinedFriends) { friend ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BedrockSurface)
                                    ) {
                                        AsyncImage(
                                            model = friend.skinUrl,
                                            contentDescription = friend.username,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = friend.username,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = { onInviteFriend(friend.minecraftUsername) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MinecraftGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Invite", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = MinecraftGreen, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = StoneSurfaceVariant
    )
}
