package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.VoiceCallBanner
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.DirectChatScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GroupChatScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.StoneSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val voiceChannels by viewModel.voiceChannels.collectAsState()

    val activeVoiceChannelId by viewModel.activeVoiceChannelId.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isDeafened by viewModel.isDeafened.collectAsState()
    val isUserSpeaking by viewModel.isUserSpeaking.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val proximityDistance by viewModel.proximityDistance.collectAsState()

    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val selectedFriendId by viewModel.selectedFriendId.collectAsState()
    val expandedImageUrl by viewModel.expandedImageUrl.collectAsState()

    val serverStatus by viewModel.serverStatus.collectAsState()
    val isPingingServer by viewModel.isPingingServer.collectAsState()

    val snackBarMessage by viewModel.snackBarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackBar()
        }
    }

    val activeChannel = voiceChannels.find { it.id == activeVoiceChannelId }

    // If user is logged out, show AuthScreen
    if (currentScreen == AppScreen.AUTH || currentUser == null) {
        AuthScreen(
            onLogin = { mc, email, name ->
                viewModel.login(mc, email, name)
            }
        )
        return
    }

    val showBottomBar = currentScreen in listOf(
        AppScreen.CHATS,
        AppScreen.VOICE,
        AppScreen.FRIENDS,
        AppScreen.PROFILE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // Persistent In-Call Voice Banner when connected and not on the Voice screen
                    if (activeChannel != null && currentScreen != AppScreen.VOICE) {
                        VoiceCallBanner(
                            channelName = activeChannel.name,
                            isMuted = isMuted,
                            isDeafened = isDeafened,
                            isUserSpeaking = isUserSpeaking,
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleDeafen = { viewModel.toggleDeafen() },
                            onDisconnect = { viewModel.leaveVoiceChannel() },
                            onClick = { viewModel.navigateTo(AppScreen.VOICE) }
                        )
                    }

                    NavigationBar(
                        containerColor = BedrockSurface,
                        contentColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        // Chats Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.CHATS,
                            onClick = { viewModel.navigateTo(AppScreen.CHATS) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "Chats"
                                )
                            },
                            label = { Text("Chats", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = MinecraftGreen,
                                indicatorColor = MinecraftGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_chats")
                        )

                        // Voice Channels Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.VOICE,
                            onClick = { viewModel.navigateTo(AppScreen.VOICE) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (activeChannel != null) {
                                            Badge(containerColor = MinecraftGreen) {
                                                Text("ON", color = Color.Black, fontSize = 8.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Voice"
                                    )
                                }
                            },
                            label = { Text("Voice VC", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = MinecraftGreen,
                                indicatorColor = MinecraftGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_voice")
                        )

                        // Friends Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.FRIENDS,
                            onClick = { viewModel.navigateTo(AppScreen.FRIENDS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (friendRequests.isNotEmpty()) {
                                            Badge(containerColor = NetherGold) {
                                                Text("${friendRequests.size}", color = Color.Black)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Friends"
                                    )
                                }
                            },
                            label = { Text("Friends", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = MinecraftGreen,
                                indicatorColor = MinecraftGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_friends")
                        )

                        // Profile Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.PROFILE,
                            onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.dp,
                                            if (currentScreen == AppScreen.PROFILE) MinecraftGreen else Color.Gray,
                                            CircleShape
                                        )
                                ) {
                                    AsyncImage(
                                        model = currentUser?.skinUrl ?: "https://mc-heads.net/avatar/Steve/64",
                                        contentDescription = "My Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            },
                            label = { Text("Profile", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = MinecraftGreen,
                                indicatorColor = MinecraftGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.CHATS -> {
                    ChatsScreen(
                        groups = groups,
                        friends = friends,
                        onOpenGroup = { groupId -> viewModel.openGroupChat(groupId) },
                        onOpenDirectChat = { friendId -> viewModel.openDirectChat(friendId) },
                        onCreateGroup = { name, desc, emoji, invitedFriends ->
                            viewModel.createGroup(name, desc, emoji, invitedFriends)
                        }
                    )
                }

                AppScreen.VOICE -> {
                    VoiceScreen(
                        channels = voiceChannels,
                        activeChannelId = activeVoiceChannelId,
                        isMuted = isMuted,
                        isDeafened = isDeafened,
                        isUserSpeaking = isUserSpeaking,
                        audioAmplitude = audioAmplitude,
                        proximityDistance = proximityDistance,
                        onJoinChannel = { chId -> viewModel.joinVoiceChannel(chId) },
                        onLeaveChannel = { viewModel.leaveVoiceChannel() },
                        onToggleMute = { viewModel.toggleMute() },
                        onToggleDeafen = { viewModel.toggleDeafen() },
                        onSetProximityDistance = { meters -> viewModel.setProximityDistance(meters) },
                        onSetParticipantVolume = { uId, vol -> viewModel.setParticipantVolume(uId, vol) }
                    )
                }

                AppScreen.FRIENDS -> {
                    FriendsScreen(
                        friends = friends,
                        friendRequests = friendRequests,
                        onOpenDirectChat = { friendId -> viewModel.openDirectChat(friendId) },
                        onSendFriendRequest = { mcName -> viewModel.sendFriendRequest(mcName) },
                        onAcceptRequest = { reqId -> viewModel.acceptFriendRequest(reqId) },
                        onDeclineRequest = { reqId -> viewModel.declineFriendRequest(reqId) },
                        onRemoveFriend = { fId -> viewModel.removeFriend(fId) }
                    )
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(
                        user = currentUser,
                        serverStatus = serverStatus,
                        isPingingServer = isPingingServer,
                        onSaveProfile = { newName, newBio, newMc, newStatus ->
                            viewModel.updateProfile(newName, newBio, newMc, newStatus)
                        },
                        onPingServer = { ip -> viewModel.pingServer(ip) },
                        onLogout = { viewModel.logout() }
                    )
                }

                AppScreen.GROUP_DETAIL -> {
                    val group = groups.find { it.id == selectedGroupId }
                    val messagesFlow = remember(selectedGroupId) {
                        viewModel.getMessagesForChannel(selectedGroupId ?: "")
                    }
                    val messages by messagesFlow.collectAsState(initial = emptyList())

                    GroupChatScreen(
                        group = group,
                        messages = messages,
                        allFriends = friends,
                        onSendMessage = { text, img ->
                            selectedGroupId?.let { viewModel.sendMessage(it, text, img) }
                        },
                        onInviteFriend = { friendMc ->
                            selectedGroupId?.let { viewModel.inviteFriendToGroup(it, friendMc) }
                        },
                        onJoinVoice = { vcId -> viewModel.joinVoiceChannel(vcId) },
                        onOpenImage = { url -> viewModel.openImageViewer(url) },
                        onBack = { viewModel.navigateTo(AppScreen.CHATS) }
                    )
                }

                AppScreen.DM_DETAIL -> {
                    val friend = friends.find { it.id == selectedFriendId }
                    val messagesFlow = remember(selectedFriendId) {
                        viewModel.getMessagesForChannel(selectedFriendId ?: "")
                    }
                    val messages by messagesFlow.collectAsState(initial = emptyList())

                    DirectChatScreen(
                        friend = friend,
                        messages = messages,
                        onSendMessage = { text, img ->
                            selectedFriendId?.let { viewModel.sendMessage(it, text, img) }
                        },
                        onStartVoiceCall = {
                            viewModel.joinVoiceChannel("vc_survival")
                            viewModel.navigateTo(AppScreen.VOICE)
                        },
                        onOpenImage = { url -> viewModel.openImageViewer(url) },
                        onBack = { viewModel.navigateTo(AppScreen.CHATS) }
                    )
                }

                AppScreen.AUTH -> {
                    AuthScreen(
                        onLogin = { mc, email, name ->
                            viewModel.login(mc, email, name)
                        }
                    )
                }
            }
        }
    }

    // Fullscreen zoomable image viewer for shared build screenshots
    if (expandedImageUrl != null) {
        ImageViewerDialog(
            imageUrl = expandedImageUrl,
            onDismiss = { viewModel.closeImageViewer() }
        )
    }
}
