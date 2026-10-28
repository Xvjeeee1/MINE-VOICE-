package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.FriendEntity
import com.example.data.local.FriendRequestEntity
import com.example.data.local.GroupEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserEntity
import com.example.data.model.ChatMessage
import com.example.data.model.Friend
import com.example.data.model.FriendRequest
import com.example.data.model.GroupChat
import com.example.data.model.GroupParticipant
import com.example.data.model.MinecraftServer
import com.example.data.model.UserAccount
import com.example.data.model.VoiceChannel
import com.example.data.model.VoiceParticipant
import com.example.data.remote.MinecraftService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID
import kotlin.random.Random

class SocialRepository(
    private val database: AppDatabase,
    private val minecraftService: MinecraftService
) {
    private val dao = database.socialDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Voice channels state
    private val _voiceChannels = MutableStateFlow<List<VoiceChannel>>(emptyList())
    val voiceChannels: StateFlow<List<VoiceChannel>> = _voiceChannels.asStateFlow()

    // Active voice participants volume overrides (userId -> volume 0..2f)
    private val _participantVolumes = MutableStateFlow<Map<String, Float>>(emptyMap())
    val participantVolumes: StateFlow<Map<String, Float>> = _participantVolumes.asStateFlow()

    init {
        scope.launch {
            seedInitialDataIfNeeded()
            initVoiceChannels()
            startVoiceRoomSpeakerSimulation()
        }
    }

    // --- User & Auth ---
    fun getCurrentUserFlow(): Flow<UserAccount?> {
        return dao.getCurrentUserFlow().map { entity ->
            entity?.let {
                UserAccount(
                    id = it.id,
                    username = it.username,
                    email = it.email,
                    minecraftUsername = it.minecraftUsername,
                    minecraftUuid = it.minecraftUuid,
                    skinUrl = if (it.skinUrl.isNotBlank()) it.skinUrl else "https://mc-heads.net/avatar/${it.minecraftUsername}/128",
                    bodySkinUrl = "https://mc-heads.net/body/${it.minecraftUsername}/256",
                    isMinecraftLinked = it.isMinecraftLinked,
                    statusText = it.statusText
                )
            }
        }
    }

    suspend fun login(minecraftUsername: String, email: String, username: String = minecraftUsername) {
        val resolved = minecraftService.resolvePlayerProfile(minecraftUsername)
        val validMcName = resolved?.first ?: minecraftUsername
        val uuid = resolved?.second ?: UUID.randomUUID().toString()

        val userEntity = UserEntity(
            id = "user_main",
            username = username.ifBlank { validMcName },
            email = email.ifBlank { "$validMcName@minecraft.net" },
            minecraftUsername = validMcName,
            minecraftUuid = uuid,
            skinUrl = "https://mc-heads.net/avatar/$validMcName/128",
            bio = "Active Minecraft adventurer & voice chatter. Love building redstone contraptions and SMPs!",
            statusText = "Mining diamonds & hanging out in VC",
            isMinecraftLinked = true
        )
        dao.insertUser(userEntity)
    }

    suspend fun updateProfile(
        username: String,
        bio: String,
        minecraftUsername: String,
        statusText: String,
        customAvatarUrl: String? = null
    ) {
        val current = dao.getCurrentUser() ?: return
        val resolved = if (minecraftUsername != current.minecraftUsername) {
            minecraftService.resolvePlayerProfile(minecraftUsername)
        } else null

        val finalMcName = resolved?.first ?: minecraftUsername
        val finalUuid = resolved?.second ?: current.minecraftUuid
        val avatar = customAvatarUrl?.ifBlank { null } ?: "https://mc-heads.net/avatar/$finalMcName/128"

        val updated = current.copy(
            username = username.ifBlank { current.username },
            bio = bio,
            minecraftUsername = finalMcName,
            minecraftUuid = finalUuid,
            skinUrl = avatar,
            statusText = statusText.ifBlank { current.statusText }
        )
        dao.updateUser(updated)
    }

    suspend fun linkMinecraftAccount(username: String): Boolean {
        val current = dao.getCurrentUser() ?: return false
        val resolved = minecraftService.resolvePlayerProfile(username)
        val finalMcName = resolved?.first ?: username
        val uuid = resolved?.second ?: ""

        dao.updateUser(
            current.copy(
                minecraftUsername = finalMcName,
                minecraftUuid = uuid,
                skinUrl = "https://mc-heads.net/avatar/$finalMcName/128",
                isMinecraftLinked = true
            )
        )
        return true
    }

    suspend fun logout() {
        dao.clearUser()
    }

    // --- Friends ---
    fun getFriendsFlow(): Flow<List<Friend>> {
        return dao.getFriendsFlow().map { list ->
            list.map {
                Friend(
                    id = it.id,
                    username = it.username,
                    minecraftUsername = it.minecraftUsername,
                    minecraftUuid = it.minecraftUuid,
                    skinUrl = if (it.skinUrl.isNotBlank()) it.skinUrl else "https://mc-heads.net/avatar/${it.minecraftUsername}/128",
                    bodySkinUrl = "https://mc-heads.net/body/${it.minecraftUsername}/256",
                    isOnline = it.isOnline,
                    activityText = it.activityText,
                    rankBadge = it.rankBadge
                )
            }
        }
    }

    fun getFriendRequestsFlow(): Flow<List<FriendRequest>> {
        return dao.getFriendRequestsFlow().map { list ->
            list.map {
                FriendRequest(
                    id = it.id,
                    senderUsername = it.senderUsername,
                    senderMinecraftUsername = it.senderMinecraftUsername,
                    senderSkinUrl = if (it.senderSkinUrl.isNotBlank()) it.senderSkinUrl else "https://mc-heads.net/avatar/${it.senderMinecraftUsername}/128",
                    timestamp = it.timestamp,
                    isIncoming = it.isIncoming
                )
            }
        }
    }

    suspend fun sendFriendRequest(targetMinecraftUsername: String): Boolean {
        val trimmed = targetMinecraftUsername.trim()
        if (trimmed.isBlank()) return false
        val resolved = minecraftService.resolvePlayerProfile(trimmed)
        val mcName = resolved?.first ?: trimmed

        val request = FriendRequestEntity(
            id = UUID.randomUUID().toString(),
            senderUsername = mcName,
            senderMinecraftUsername = mcName,
            senderSkinUrl = "https://mc-heads.net/avatar/$mcName/128",
            timestamp = System.currentTimeMillis(),
            isIncoming = false
        )
        dao.insertFriendRequest(request)
        return true
    }

    suspend fun acceptFriendRequest(requestId: String) {
        val requests = dao.getFriendRequestsFlow()
        // Find request
        withContext(Dispatchers.IO) {
            dao.deleteFriendRequest(requestId)
            // Add as friend
            val friendId = "friend_${UUID.randomUUID()}"
            val newFriend = FriendEntity(
                id = friendId,
                username = "TechnoBlade",
                minecraftUsername = "Technoblade",
                skinUrl = "https://mc-heads.net/avatar/Technoblade/128",
                isOnline = true,
                activityText = "Playing Bedwars",
                rankBadge = "MVP++"
            )
            dao.insertFriend(newFriend)
            // Send initial welcome message
            dao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    channelId = friendId,
                    senderId = friendId,
                    senderUsername = newFriend.username,
                    senderMinecraftUsername = newFriend.minecraftUsername,
                    senderSkinUrl = newFriend.skinUrl,
                    text = "Hey! Thanks for adding me. Down to hop on VC and play Bedwars?",
                    imageUrl = null,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun declineFriendRequest(requestId: String) {
        dao.deleteFriendRequest(requestId)
    }

    suspend fun removeFriend(friendId: String) {
        dao.deleteFriend(friendId)
    }

    // --- Groups ---
    fun getGroupsFlow(): Flow<List<GroupChat>> {
        return dao.getGroupsFlow().map { groups ->
            groups.map { entity ->
                val participants = parseParticipants(entity.participantsJson)
                GroupChat(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    iconEmoji = entity.iconEmoji,
                    participants = participants,
                    lastMessage = entity.lastMessage,
                    lastMessageTime = entity.lastMessageTime,
                    voiceChannelId = entity.voiceChannelId
                )
            }
        }
    }

    private suspend fun parseParticipants(json: String): List<GroupParticipant> {
        val list = mutableListOf<GroupParticipant>()
        val currentUser = dao.getCurrentUser()
        if (currentUser != null) {
            list.add(
                GroupParticipant(
                    userId = currentUser.id,
                    username = currentUser.username,
                    minecraftUsername = currentUser.minecraftUsername,
                    skinUrl = currentUser.skinUrl,
                    role = "Owner",
                    isOnline = true
                )
            )
        }
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val pName = jsonArray.getString(i)
                list.add(
                    GroupParticipant(
                        userId = "p_$pName",
                        username = pName,
                        minecraftUsername = pName,
                        skinUrl = "https://mc-heads.net/avatar/$pName/128",
                        role = if (i == 0) "Admin" else "Member",
                        isOnline = i % 2 == 0
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    suspend fun createGroupChat(
        name: String,
        description: String,
        iconEmoji: String,
        participantFriendNames: List<String>
    ) {
        val groupId = "group_${UUID.randomUUID()}"
        val array = JSONArray()
        participantFriendNames.forEach { array.put(it) }

        val entity = GroupEntity(
            id = groupId,
            name = name.ifBlank { "Minecraft Squad" },
            description = description.ifBlank { "Minecraft voice and chat hub" },
            iconEmoji = iconEmoji.ifBlank { "⚔️" },
            participantsJson = array.toString(),
            lastMessage = "Group created! Welcome to $name",
            lastMessageTime = "Just now",
            voiceChannelId = "vc_survival"
        )
        dao.insertGroup(entity)

        val currentUser = dao.getCurrentUser()
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                channelId = groupId,
                senderId = currentUser?.id ?: "me",
                senderUsername = currentUser?.username ?: "You",
                senderMinecraftUsername = currentUser?.minecraftUsername ?: "Steve",
                senderSkinUrl = currentUser?.skinUrl ?: "https://mc-heads.net/avatar/Steve/128",
                text = "Welcome everyone to $name! Feel free to share screenshots and hop in VC.",
                imageUrl = null,
                timestamp = System.currentTimeMillis(),
                isMine = true
            )
        )
    }

    suspend fun inviteFriendToGroup(groupId: String, friendMinecraftUsername: String) {
        val group = dao.getGroupById(groupId) ?: return
        val array = try {
            JSONArray(group.participantsJson)
        } catch (_: Exception) {
            JSONArray()
        }
        var exists = false
        for (i in 0 until array.length()) {
            if (array.getString(i).equals(friendMinecraftUsername, ignoreCase = true)) {
                exists = true
                break
            }
        }
        if (!exists) {
            array.put(friendMinecraftUsername)
            dao.updateGroup(group.copy(participantsJson = array.toString()))
            val currentUser = dao.getCurrentUser()
            dao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    channelId = groupId,
                    senderId = "system",
                    senderUsername = "Server System",
                    senderMinecraftUsername = "Command_Block",
                    senderSkinUrl = "https://mc-heads.net/avatar/MHF_CommandBlock/128",
                    text = "${currentUser?.username ?: "Someone"} invited $friendMinecraftUsername to the group!",
                    imageUrl = null,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Messages & Image Sharing ---
    fun getMessagesFlow(channelId: String): Flow<List<ChatMessage>> {
        return dao.getMessagesFlow(channelId).map { messages ->
            val currentUserId = dao.getCurrentUser()?.id ?: "user_main"
            messages.map {
                ChatMessage(
                    id = it.id,
                    channelId = it.channelId,
                    senderId = it.senderId,
                    senderUsername = it.senderUsername,
                    senderMinecraftUsername = it.senderMinecraftUsername,
                    senderSkinUrl = it.senderSkinUrl,
                    text = it.text,
                    imageUrl = it.imageUrl,
                    timestamp = it.timestamp,
                    isMine = it.senderId == currentUserId || it.isMine
                )
            }
        }
    }

    suspend fun sendMessage(channelId: String, text: String, imageUrl: String? = null) {
        val currentUser = dao.getCurrentUser() ?: return
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            senderId = currentUser.id,
            senderUsername = currentUser.username,
            senderMinecraftUsername = currentUser.minecraftUsername,
            senderSkinUrl = currentUser.skinUrl,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        dao.insertMessage(message)

        // Update group last message if it's a group
        val group = dao.getGroupById(channelId)
        if (group != null) {
            val preview = if (imageUrl != null) "📷 Shared an image" else text
            dao.updateGroup(
                group.copy(
                    lastMessage = preview,
                    lastMessageTime = "Just now"
                )
            )
        }
    }

    // --- Voice Channels ---
    private fun initVoiceChannels() {
        val defaultChannels = listOf(
            VoiceChannel(
                id = "vc_survival",
                name = "Survival SMP Proximity",
                description = "Proximity audio (15m radius) for our main survival base",
                isProximity = true,
                serverName = "Survival Craft SMP",
                participants = listOf(
                    VoiceParticipant("p_alex", "Alex_Crafter", "Alex", isSpeaking = true, distanceMeters = 8),
                    VoiceParticipant("p_notch", "Notch", "Notch", isSpeaking = false, distanceMeters = 14),
                    VoiceParticipant("p_steve", "StevePro", "Steve", isSpeaking = false, distanceMeters = 22)
                )
            ),
            VoiceChannel(
                id = "vc_bedwars",
                name = "Bedwars Squad #1",
                description = "Tactical callouts & rush strategy",
                isProximity = false,
                serverName = "Hypixel Network",
                participants = listOf(
                    VoiceParticipant("p_techno", "TechnoBlade", "Technoblade", isSpeaking = false, distanceMeters = 0),
                    VoiceParticipant("p_dream", "Dream_Speed", "Dream", isSpeaking = true, distanceMeters = 0)
                )
            ),
            VoiceChannel(
                id = "vc_redstone",
                name = "Redstone Workshop",
                description = "Building 60hz sorting systems and flying machines",
                isProximity = false,
                serverName = "Redstone Labs",
                participants = listOf(
                    VoiceParticipant("p_mumbo", "Mumbo_Jumbo", "MumboJumbo", isSpeaking = true, distanceMeters = 0)
                )
            ),
            VoiceChannel(
                id = "vc_nether",
                name = "Nether Bastion Raid",
                description = "Watch out for Piglin Brutes! Proximity 25m",
                isProximity = true,
                serverName = "Survival Craft SMP",
                participants = listOf(
                    VoiceParticipant("p_jeb", "Jeb_", "jeb_", isSpeaking = false, distanceMeters = 10)
                )
            )
        )
        _voiceChannels.value = defaultChannels
    }

    fun setParticipantVolume(userId: String, volume: Float) {
        val current = _participantVolumes.value.toMutableMap()
        current[userId] = volume
        _participantVolumes.value = current
    }

    private fun startVoiceRoomSpeakerSimulation() {
        scope.launch {
            while (isActive) {
                delay(2400)
                val channels = _voiceChannels.value.map { ch ->
                    val updatedParticipants = ch.participants.map { p ->
                        val shouldSpeak = Random.nextFloat() > 0.65f
                        p.copy(isSpeaking = shouldSpeak)
                    }
                    ch.copy(participants = updatedParticipants)
                }
                _voiceChannels.value = channels
            }
        }
    }

    suspend fun getServerStatus(ip: String): MinecraftServer {
        return minecraftService.pingServer(ip)
    }

    // --- Seeding ---
    private suspend fun seedInitialDataIfNeeded() {
        if (dao.getCurrentUser() == null) {
            val defaultUser = UserEntity(
                id = "user_main",
                username = "EnderKnight",
                email = "enderknight@minecraft.net",
                minecraftUsername = "Notch",
                minecraftUuid = "069a79f4-44e9-4726-a5be-fca90e38aaf5",
                skinUrl = "https://mc-heads.net/avatar/Notch/128",
                bio = "Minecraft builder, SMP veteran & redstone enthusiast. Always down for VC and bedwars!",
                statusText = "Mining ancient debris in Nether",
                isMinecraftLinked = true
            )
            dao.insertUser(defaultUser)
        }

        // Friends
        val existingFriends = dao.getFriendsFlow()
        val defaultFriends = listOf(
            FriendEntity(
                id = "friend_alex",
                username = "Alex_Crafter",
                minecraftUsername = "Alex",
                skinUrl = "https://mc-heads.net/avatar/Alex/128",
                isOnline = true,
                activityText = "In Survival VC (8m)",
                rankBadge = "VIP+"
            ),
            FriendEntity(
                id = "friend_dream",
                username = "Dream_Speed",
                minecraftUsername = "Dream",
                skinUrl = "https://mc-heads.net/avatar/Dream/128",
                isOnline = true,
                activityText = "Playing Bedwars Squad",
                rankBadge = "MVP+"
            ),
            FriendEntity(
                id = "friend_mumbo",
                username = "Mumbo_Jumbo",
                minecraftUsername = "MumboJumbo",
                skinUrl = "https://mc-heads.net/avatar/MumboJumbo/128",
                isOnline = true,
                activityText = "In Redstone Workshop VC",
                rankBadge = "Architect"
            ),
            FriendEntity(
                id = "friend_steve",
                username = "SteveTheMiner",
                minecraftUsername = "Steve",
                skinUrl = "https://mc-heads.net/avatar/Steve/128",
                isOnline = false,
                activityText = "Offline (last seen 2h ago)",
                rankBadge = "Member"
            )
        )
        dao.insertFriends(defaultFriends)

        // Pending Friend Requests
        dao.insertFriendRequest(
            FriendRequestEntity(
                id = "req_techno",
                senderUsername = "TechnoBlade",
                senderMinecraftUsername = "Technoblade",
                senderSkinUrl = "https://mc-heads.net/avatar/Technoblade/128",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 35,
                isIncoming = true
            )
        )
        dao.insertFriendRequest(
            FriendRequestEntity(
                id = "req_jeb",
                senderUsername = "Jeb_Builder",
                senderMinecraftUsername = "jeb_",
                senderSkinUrl = "https://mc-heads.net/avatar/jeb_/128",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                isIncoming = true
            )
        )

        // Groups
        val groupSMP = GroupEntity(
            id = "group_smp",
            name = "Survival SMP Realm",
            description = "Main survival group. Rules: No griefing, keep inventory OFF, proximity VC on!",
            iconEmoji = "🌲",
            participantsJson = JSONArray(listOf("Alex", "Notch", "Steve", "MumboJumbo")).toString(),
            lastMessage = "Check out the new castle build at coordinates X: 420 Z: -850!",
            lastMessageTime = "10m ago",
            voiceChannelId = "vc_survival"
        )
        val groupBedwars = GroupEntity(
            id = "group_bedwars",
            name = "Bedwars Tryhards",
            description = "Diamond generators rushers & 4v4 streak grinders",
            iconEmoji = "⚔️",
            participantsJson = JSONArray(listOf("Dream", "Technoblade", "Alex")).toString(),
            lastMessage = "Who is ready for Bedwars squad party right now?",
            lastMessageTime = "25m ago",
            voiceChannelId = "vc_bedwars"
        )
        val groupRedstone = GroupEntity(
            id = "group_redstone",
            name = "Redstone Engineers",
            description = "Piston doors, shulker unloader schemes, and zero-tick farms",
            iconEmoji = "⚡",
            participantsJson = JSONArray(listOf("MumboJumbo", "jeb_")).toString(),
            lastMessage = "I finished the 3x3 seamless flush glass door!",
            lastMessageTime = "1h ago",
            voiceChannelId = "vc_redstone"
        )
        dao.insertGroups(listOf(groupSMP, groupBedwars, groupRedstone))

        // Preload rich messages
        dao.insertMessages(
            listOf(
                MessageEntity(
                    id = "msg_smp_1",
                    channelId = "group_smp",
                    senderId = "p_alex",
                    senderUsername = "Alex_Crafter",
                    senderMinecraftUsername = "Alex",
                    senderSkinUrl = "https://mc-heads.net/avatar/Alex/128",
                    text = "Welcome to the Survival SMP chat! Remember to keep proximity voice chat on when exploring near spawn.",
                    imageUrl = null,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60
                ),
                MessageEntity(
                    id = "msg_smp_2",
                    channelId = "group_smp",
                    senderId = "p_alex",
                    senderUsername = "Alex_Crafter",
                    senderMinecraftUsername = "Alex",
                    senderSkinUrl = "https://mc-heads.net/avatar/Alex/128",
                    text = "Look at the epic castle gatehouse we completed today!",
                    imageUrl = "https://images.unsplash.com/photo-1627856013091-fed6e4e30025?w=800&auto=format&fit=crop&q=80",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30
                ),
                MessageEntity(
                    id = "msg_dm_alex_1",
                    channelId = "friend_alex",
                    senderId = "friend_alex",
                    senderUsername = "Alex_Crafter",
                    senderMinecraftUsername = "Alex",
                    senderSkinUrl = "https://mc-heads.net/avatar/Alex/128",
                    text = "Hey! Want to hop into the Survival SMP Voice Channel? We're trading with librarians right now.",
                    imageUrl = null,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15
                )
            )
        )
    }
}
