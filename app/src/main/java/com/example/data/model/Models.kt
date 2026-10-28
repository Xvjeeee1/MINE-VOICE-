package com.example.data.model

data class UserAccount(
    val id: String,
    val username: String,
    val email: String,
    val minecraftUsername: String,
    val minecraftUuid: String = "",
    val skinUrl: String = "https://mc-heads.net/avatar/$minecraftUsername/128",
    val bodySkinUrl: String = "https://mc-heads.net/body/$minecraftUsername/256",
    val isMinecraftLinked: Boolean = true,
    val statusText: String = "Mining diamonds & hanging out in VC",
    val isOnline: Boolean = true
)

data class Friend(
    val id: String,
    val username: String,
    val minecraftUsername: String,
    val minecraftUuid: String = "",
    val skinUrl: String = "https://mc-heads.net/avatar/$minecraftUsername/128",
    val bodySkinUrl: String = "https://mc-heads.net/body/$minecraftUsername/256",
    val isOnline: Boolean = true,
    val activityText: String = "Survival SMP",
    val rankBadge: String = "VIP"
)

data class FriendRequest(
    val id: String,
    val senderUsername: String,
    val senderMinecraftUsername: String,
    val senderSkinUrl: String = "https://mc-heads.net/avatar/$senderMinecraftUsername/128",
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = true
)

data class GroupParticipant(
    val userId: String,
    val username: String,
    val minecraftUsername: String,
    val skinUrl: String = "https://mc-heads.net/avatar/$minecraftUsername/128",
    val role: String = "Member", // Owner, Admin, Member
    val isOnline: Boolean = true
)

data class GroupChat(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String = "⚔️",
    val participants: List<GroupParticipant>,
    val lastMessage: String = "",
    val lastMessageTime: String = "Just now",
    val unreadCount: Int = 0,
    val voiceChannelId: String? = null
)

data class ChatMessage(
    val id: String,
    val channelId: String, // groupId or friendId for DM
    val senderId: String,
    val senderUsername: String,
    val senderMinecraftUsername: String,
    val senderSkinUrl: String = "https://mc-heads.net/avatar/$senderMinecraftUsername/128",
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = false
)

data class VoiceParticipant(
    val userId: String,
    val username: String,
    val minecraftUsername: String,
    val skinUrl: String = "https://mc-heads.net/avatar/$minecraftUsername/128",
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isSpeaking: Boolean = false,
    val volume: Float = 1.0f,
    val distanceMeters: Int = 12 // For Minecraft proximity audio
)

data class VoiceChannel(
    val id: String,
    val name: String,
    val description: String,
    val isProximity: Boolean = false,
    val serverName: String = "CraftVoice Server",
    val participants: List<VoiceParticipant> = emptyList(),
    val maxParticipants: Int = 20
)

data class MinecraftServer(
    val ip: String,
    val name: String,
    val motd: String,
    val onlinePlayers: Int,
    val maxPlayers: Int,
    val pingMs: Int,
    val version: String,
    val isOnline: Boolean = true
)
