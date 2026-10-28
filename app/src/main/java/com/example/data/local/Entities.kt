package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val minecraftUsername: String,
    val minecraftUuid: String = "",
    val skinUrl: String = "",
    val bio: String = "Minecraft builder & Redstone enthusiast",
    val statusText: String = "Mining diamonds & hanging out in VC",
    val isMinecraftLinked: Boolean = true
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val username: String,
    val minecraftUsername: String,
    val minecraftUuid: String = "",
    val skinUrl: String = "",
    val isOnline: Boolean = true,
    val activityText: String = "Playing Hypixel",
    val rankBadge: String = "VIP"
)

@Entity(tableName = "friend_requests")
data class FriendRequestEntity(
    @PrimaryKey val id: String,
    val senderUsername: String,
    val senderMinecraftUsername: String,
    val senderSkinUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = true
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String = "⚔️",
    val participantsJson: String = "", // JSON list of participant IDs
    val lastMessage: String = "",
    val lastMessageTime: String = "Just now",
    val voiceChannelId: String? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val channelId: String, // group id or friend id
    val senderId: String,
    val senderUsername: String,
    val senderMinecraftUsername: String,
    val senderSkinUrl: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = false
)
