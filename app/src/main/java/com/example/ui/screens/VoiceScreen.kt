package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.VoiceChannel
import com.example.data.model.VoiceParticipant
import com.example.ui.components.ProximityRadarView
import com.example.ui.components.VoiceParticipantCard
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.SpeakingHalo
import com.example.ui.theme.StoneSurfaceVariant

@Composable
fun VoiceScreen(
    channels: List<VoiceChannel>,
    activeChannelId: String?,
    isMuted: Boolean,
    isDeafened: Boolean,
    isUserSpeaking: Boolean,
    audioAmplitude: Float,
    proximityDistance: Int,
    onJoinChannel: (String) -> Unit,
    onLeaveChannel: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onSetProximityDistance: (Int) -> Unit,
    onSetParticipantVolume: (userId: String, volume: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val activeChannel = channels.find { it.id == activeChannelId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(16.dp)
            .testTag("voice_screen")
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Minecraft Voice Channels",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time voice with Minecraft spatial proximity audio",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeChannel != null) MinecraftGreen.copy(alpha = 0.2f) else StoneSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (activeChannel != null) MinecraftGreen else MinecraftBorder)
                ) {
                    Text(
                        text = if (activeChannel != null) "CONNECTED" else "STANDBY",
                        color = if (activeChannel != null) MinecraftGreen else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Voice Room Console (if connected)
        if (activeChannel != null) {
            item {
                ActiveVoiceRoomConsole(
                    channel = activeChannel,
                    isMuted = isMuted,
                    isDeafened = isDeafened,
                    isUserSpeaking = isUserSpeaking,
                    audioAmplitude = audioAmplitude,
                    proximityDistance = proximityDistance,
                    onToggleMute = onToggleMute,
                    onToggleDeafen = onToggleDeafen,
                    onLeaveChannel = onLeaveChannel,
                    onSetProximityDistance = onSetProximityDistance,
                    onRequestMicPermission = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Proximity Radar if channel has proximity enabled
                if (activeChannel.isProximity) {
                    ProximityRadarView(
                        proximityRadiusMeters = proximityDistance,
                        participants = activeChannel.participants,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "PARTICIPANTS IN CHANNEL (${activeChannel.participants.size})",
                    color = DiamondCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(activeChannel.participants) { participant ->
                VoiceParticipantCard(
                    participant = participant,
                    volume = 1.0f,
                    onVolumeChange = { vol -> onSetParticipantVolume(participant.userId, vol) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SWITCH TO OTHER CHANNELS",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // List of all channels
        items(channels) { channel ->
            val isCurrent = channel.id == activeChannelId
            VoiceChannelCard(
                channel = channel,
                isConnected = isCurrent,
                onJoin = { onJoinChannel(channel.id) },
                onLeave = onLeaveChannel
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ActiveVoiceRoomConsole(
    channel: VoiceChannel,
    isMuted: Boolean,
    isDeafened: Boolean,
    isUserSpeaking: Boolean,
    audioAmplitude: Float,
    proximityDistance: Int,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onLeaveChannel: () -> Unit,
    onSetProximityDistance: (Int) -> Unit,
    onRequestMicPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BedrockSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinecraftGreen)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MinecraftGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = channel.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                    Text(
                        text = "${channel.serverName} • ${if (channel.isProximity) "Spatial Proximity Audio" else "Global Audio"}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onLeaveChannel,
                    colors = ButtonDefaults.buttonColors(containerColor = RedstoneRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Leave Channel",
                        tint = RedstoneRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Leave", color = RedstoneRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Level Waveform Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StoneSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isUserSpeaking) SpeakingHalo else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMuted) "Microphone Muted" else if (isUserSpeaking) "Speaking (Voice Detected)" else "Listening...",
                        color = if (isUserSpeaking) SpeakingHalo else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BedrockSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = if (isMuted) 0f else audioAmplitude.coerceIn(0.05f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isUserSpeaking) SpeakingHalo else MinecraftGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proximity range slider if proximity channel
            if (channel.isProximity) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Proximity Radius: ${proximityDistance}m",
                            color = DiamondCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Simulates Minecraft proximity mod",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = proximityDistance.toFloat(),
                        onValueChange = { onSetProximityDistance(it.toInt()) },
                        valueRange = 4f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = DiamondCyan,
                            activeTrackColor = DiamondCyan,
                            inactiveTrackColor = StoneSurfaceVariant
                        )
                    )
                }
            }

            // Quick Mute / Deafen Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleMute,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMuted) RedstoneRed.copy(alpha = 0.25f) else StoneSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isMuted) RedstoneRed else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMuted) "Unmute" else "Mute Mic",
                        color = if (isMuted) RedstoneRed else Color.White,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onToggleDeafen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeafened) RedstoneRed.copy(alpha = 0.25f) else StoneSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                        contentDescription = null,
                        tint = if (isDeafened) RedstoneRed else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDeafened) "Undeafen" else "Deafen",
                        color = if (isDeafened) RedstoneRed else Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceChannelCard(
    channel: VoiceChannel,
    isConnected: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_channel_card_${channel.id}"),
        colors = CardDefaults.cardColors(containerColor = BedrockSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) MinecraftGreen else MinecraftBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isConnected) MinecraftGreen.copy(alpha = 0.2f) else StoneSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isConnected) MinecraftGreen else DiamondCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = channel.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel.serverName,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            if (channel.isProximity) {
                                Text(
                                    text = " • Proximity Audio",
                                    color = NetherGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (isConnected) {
                    Button(
                        onClick = onLeave,
                        colors = ButtonDefaults.buttonColors(containerColor = RedstoneRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Disconnect", color = RedstoneRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = MinecraftGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("join_vc_btn_${channel.id}")
                    ) {
                        Text("Join VC", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = channel.description,
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Participants in channel previews
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${channel.participants.size}/${channel.maxParticipants} connected:",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                channel.participants.take(5).forEach { p ->
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(1.dp, if (p.isSpeaking) SpeakingHalo else MinecraftBorder, CircleShape)
                    ) {
                        AsyncImage(
                            model = p.skinUrl,
                            contentDescription = p.username,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
