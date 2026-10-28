package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.Friend
import com.example.data.model.FriendRequest
import com.example.data.model.GroupChat
import com.example.data.model.MinecraftServer
import com.example.data.model.UserAccount
import com.example.data.model.VoiceChannel
import com.example.data.remote.MinecraftService
import com.example.data.repository.SocialRepository
import com.example.voice.VoiceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    CHATS,
    VOICE,
    FRIENDS,
    PROFILE,
    GROUP_DETAIL,
    DM_DETAIL,
    AUTH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val minecraftService = MinecraftService()
    val repository = SocialRepository(database, minecraftService)
    val voiceEngine = VoiceEngine(application)

    // Navigation & Screen Selection
    private val _currentScreen = MutableStateFlow(AppScreen.CHATS)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    private val _selectedFriendId = MutableStateFlow<String?>(null)
    val selectedFriendId: StateFlow<String?> = _selectedFriendId.asStateFlow()

    private val _selectedVoiceChannelId = MutableStateFlow<String?>(null)
    val selectedVoiceChannelId: StateFlow<String?> = _selectedVoiceChannelId.asStateFlow()

    // Fullscreen image preview
    private val _expandedImageUrl = MutableStateFlow<String?>(null)
    val expandedImageUrl: StateFlow<String?> = _expandedImageUrl.asStateFlow()

    // User & Data Flows
    val currentUser: StateFlow<UserAccount?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val friends: StateFlow<List<Friend>> = repository.getFriendsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendRequests: StateFlow<List<FriendRequest>> = repository.getFriendRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<GroupChat>> = repository.getGroupsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceChannels: StateFlow<List<VoiceChannel>> = repository.voiceChannels

    val isMuted = voiceEngine.isMuted
    val isDeafened = voiceEngine.isDeafened
    val isUserSpeaking = voiceEngine.isUserSpeaking
    val audioAmplitude = voiceEngine.audioAmplitude
    val proximityDistance = voiceEngine.proximityDistance
    val activeVoiceChannelId = voiceEngine.activeChannelId

    // Server Ping
    private val _serverStatus = MutableStateFlow<MinecraftServer?>(null)
    val serverStatus: StateFlow<MinecraftServer?> = _serverStatus.asStateFlow()

    private val _isPingingServer = MutableStateFlow(false)
    val isPingingServer: StateFlow<Boolean> = _isPingingServer.asStateFlow()

    // Status snackbar message
    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    init {
        pingServer("mc.hypixel.net")
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openGroupChat(groupId: String) {
        _selectedGroupId.value = groupId
        _currentScreen.value = AppScreen.GROUP_DETAIL
    }

    fun openDirectChat(friendId: String) {
        _selectedFriendId.value = friendId
        _currentScreen.value = AppScreen.DM_DETAIL
    }

    fun openVoiceChannelDetail(channelId: String) {
        _selectedVoiceChannelId.value = channelId
        _currentScreen.value = AppScreen.VOICE
    }

    fun openImageViewer(url: String) {
        _expandedImageUrl.value = url
    }

    fun closeImageViewer() {
        _expandedImageUrl.value = null
    }

    fun showMessage(msg: String) {
        _snackBarMessage.value = msg
    }

    fun clearSnackBar() {
        _snackBarMessage.value = null
    }

    // --- Messages ---
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessage>> {
        return repository.getMessagesFlow(channelId)
    }

    fun sendMessage(channelId: String, text: String, imageUrl: String? = null) {
        if (text.isBlank() && imageUrl.isNullOrBlank()) return
        viewModelScope.launch {
            repository.sendMessage(channelId, text.trim(), imageUrl)
        }
    }

    // --- Groups ---
    fun createGroup(name: String, description: String, iconEmoji: String, friendUsernames: List<String>) {
        viewModelScope.launch {
            repository.createGroupChat(name, description, iconEmoji, friendUsernames)
            showMessage("Created group '$name'!")
        }
    }

    fun inviteFriendToGroup(groupId: String, friendMinecraftUsername: String) {
        viewModelScope.launch {
            repository.inviteFriendToGroup(groupId, friendMinecraftUsername)
            showMessage("Invited $friendMinecraftUsername to the group!")
        }
    }

    // --- Friends ---
    fun sendFriendRequest(targetMinecraftUsername: String) {
        viewModelScope.launch {
            val success = repository.sendFriendRequest(targetMinecraftUsername)
            if (success) {
                showMessage("Friend request sent to $targetMinecraftUsername!")
            } else {
                showMessage("Could not find Minecraft player: $targetMinecraftUsername")
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(requestId)
            showMessage("Friend request accepted!")
        }
    }

    fun declineFriendRequest(requestId: String) {
        viewModelScope.launch {
            repository.declineFriendRequest(requestId)
            showMessage("Friend request declined.")
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repository.removeFriend(friendId)
            showMessage("Friend removed.")
        }
    }

    // --- Profile ---
    fun updateProfile(
        username: String,
        bio: String,
        minecraftUsername: String,
        statusText: String,
        customAvatarUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.updateProfile(username, bio, minecraftUsername, statusText, customAvatarUrl)
            showMessage("Profile updated successfully!")
        }
    }

    fun linkMinecraftAccount(username: String) {
        viewModelScope.launch {
            val ok = repository.linkMinecraftAccount(username)
            if (ok) {
                showMessage("Minecraft account linked to $username!")
            }
        }
    }

    fun login(minecraftUsername: String, email: String, username: String) {
        viewModelScope.launch {
            repository.login(minecraftUsername, email, username)
            _currentScreen.value = AppScreen.CHATS
            showMessage("Welcome, $username!")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = AppScreen.AUTH
        }
    }

    // --- Voice Controls ---
    fun joinVoiceChannel(channelId: String) {
        voiceEngine.joinChannel(channelId)
        _selectedVoiceChannelId.value = channelId
        showMessage("Connected to Voice Channel!")
    }

    fun leaveVoiceChannel() {
        voiceEngine.leaveChannel()
        showMessage("Disconnected from Voice Channel.")
    }

    fun toggleMute() {
        voiceEngine.toggleMute()
    }

    fun toggleDeafen() {
        voiceEngine.toggleDeafen()
    }

    fun setProximityDistance(meters: Int) {
        voiceEngine.setProximityDistance(meters)
    }

    fun setParticipantVolume(userId: String, volume: Float) {
        repository.setParticipantVolume(userId, volume)
    }

    // --- Server Ping ---
    fun pingServer(serverAddress: String) {
        viewModelScope.launch {
            _isPingingServer.value = true
            val res = repository.getServerStatus(serverAddress)
            _serverStatus.value = res
            _isPingingServer.value = false
        }
    }
}
