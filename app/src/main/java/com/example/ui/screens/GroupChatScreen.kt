package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Friend
import com.example.data.model.GroupChat
import com.example.ui.components.ChatInputBar
import com.example.ui.components.MessageBubble
import com.example.ui.dialogs.GroupParticipantsDialog
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.StoneSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    group: GroupChat?,
    messages: List<ChatMessage>,
    allFriends: List<Friend>,
    onSendMessage: (text: String, imageUrl: String?) -> Unit,
    onInviteFriend: (friendMinecraftUsername: String) -> Unit,
    onJoinVoice: (String) -> Unit,
    onOpenImage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var showParticipantsDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (group == null) {
        Box(modifier = Modifier.fillMaxSize().background(ObsidianDark))
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StoneSurfaceVariant)
                            .border(1.dp, MinecraftBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = group.iconEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${group.participants.size} participants • ${group.description}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_group_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                // Join Voice Quick Action
                if (group.voiceChannelId != null) {
                    IconButton(
                        onClick = { onJoinVoice(group.voiceChannelId) },
                        modifier = Modifier.testTag("group_join_vc_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Join VC",
                            tint = MinecraftGreen
                        )
                    }
                }

                // Participants & Invite Button
                IconButton(
                    onClick = { showParticipantsDialog = true },
                    modifier = Modifier.testTag("group_participants_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "View Participants and Invite",
                        tint = DiamondCyan
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BedrockSurface,
                titleContentColor = Color.White
            )
        )

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    onImageClick = onOpenImage
                )
            }
        }

        // Input bar with photo picker and screenshots gallery
        ChatInputBar(
            onSendMessage = onSendMessage
        )
    }

    if (showParticipantsDialog) {
        GroupParticipantsDialog(
            group = group,
            allFriends = allFriends,
            onInviteFriend = onInviteFriend,
            onDismiss = { showParticipantsDialog = false }
        )
    }
}
