package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.FriendRequest
import com.example.ui.dialogs.AddFriendDialog
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.StoneSurfaceVariant

@Composable
fun FriendsScreen(
    friends: List<Friend>,
    friendRequests: List<FriendRequest>,
    onOpenDirectChat: (friendId: String) -> Unit,
    onSendFriendRequest: (minecraftUsername: String) -> Unit,
    onAcceptRequest: (requestId: String) -> Unit,
    onDeclineRequest: (requestId: String) -> Unit,
    onRemoveFriend: (friendId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tabs: All Friends vs Pending Requests
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = BedrockSurface,
                contentColor = MinecraftGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MinecraftGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Friends (${friends.size})",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    selectedContentColor = MinecraftGreen,
                    unselectedContentColor = Color.Gray,
                    modifier = Modifier.testTag("tab_all_friends")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pending",
                                fontWeight = FontWeight.Bold
                            )
                            if (friendRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NetherGold,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${friendRequests.size}",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    selectedContentColor = MinecraftGreen,
                    unselectedContentColor = Color.Gray,
                    modifier = Modifier.testTag("tab_pending_requests")
                )
            }

            if (selectedTab == 0) {
                // Friends List
                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No friends added yet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap the + button to search and add friends by their Minecraft username.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        items(friends) { friend ->
                            FriendItemCard(
                                friend = friend,
                                onMessageClick = { onOpenDirectChat(friend.id) },
                                onRemoveClick = { onRemoveFriend(friend.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } else {
                // Friend Requests List
                if (friendRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No pending friend requests", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("When someone sends you a friend request, it will appear here.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        items(friendRequests) { req ->
                            FriendRequestCard(
                                request = req,
                                onAccept = { onAcceptRequest(req.id) },
                                onDecline = { onDeclineRequest(req.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Add Friend FAB
        FloatingActionButton(
            onClick = { showAddFriendDialog = true },
            containerColor = MinecraftGreen,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_friend_fab")
        ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Friend")
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onSendRequest = { username ->
                onSendFriendRequest(username)
                showAddFriendDialog = false
            },
            onDismiss = { showAddFriendDialog = false }
        )
    }
}

@Composable
fun FriendItemCard(
    friend: Friend,
    onMessageClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_card_${friend.id}"),
        colors = CardDefaults.cardColors(containerColor = BedrockSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinecraftBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StoneSurfaceVariant)
                    .border(1.dp, MinecraftBorder, RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = friend.skinUrl,
                    contentDescription = friend.username,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (friend.isOnline) MinecraftGreen else Color.Gray)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.username,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${friend.minecraftUsername})",
                        color = DiamondCyan,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.activityText,
                        color = if (friend.isOnline) Color.LightGray else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onMessageClick,
                modifier = Modifier.testTag("dm_friend_${friend.id}_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = "Message Friend",
                    tint = MinecraftGreen
                )
            }

            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.testTag("remove_friend_${friend.id}_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Friend",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_request_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = BedrockSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetherGold.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StoneSurfaceVariant)
                        .border(1.dp, MinecraftBorder, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = request.senderSkinUrl,
                        contentDescription = request.senderUsername,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = request.senderUsername,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "MC: ${request.senderMinecraftUsername}",
                        color = DiamondCyan,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Wants to be your friend",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .background(MinecraftGreen.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                        .testTag("accept_req_${request.id}_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept Request",
                        tint = MinecraftGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .background(RedstoneRed.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                        .testTag("decline_req_${request.id}_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline Request",
                        tint = RedstoneRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
