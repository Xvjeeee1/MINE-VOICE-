package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VoiceParticipant
import com.example.ui.theme.BedrockSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.MinecraftBorder
import com.example.ui.theme.MinecraftGreen
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.SpeakingHalo
import com.example.ui.theme.StoneSurfaceVariant

@Composable
fun VoiceCallBanner(
    channelName: String,
    isMuted: Boolean,
    isDeafened: Boolean,
    isUserSpeaking: Boolean,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onDisconnect: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("voice_call_banner"),
        color = BedrockSurface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MinecraftBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isUserSpeaking) SpeakingHalo.copy(alpha = 0.2f) else MinecraftGreen.copy(alpha = 0.15f),
                            CircleShape
                        )
                        .border(
                            width = if (isUserSpeaking) 2.dp else 1.dp,
                            color = if (isUserSpeaking) SpeakingHalo else MinecraftGreen,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Active",
                        tint = if (isUserSpeaking) SpeakingHalo else MinecraftGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = channelName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isDeafened) "Deafened" else if (isMuted) "Muted" else if (isUserSpeaking) "Speaking..." else "Connected • Voice Active",
                        color = if (isUserSpeaking) SpeakingHalo else DiamondCyan,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.testTag("banner_mute_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) RedstoneRed else Color.White
                    )
                }

                IconButton(
                    onClick = onToggleDeafen,
                    modifier = Modifier.testTag("banner_deafen_btn")
                ) {
                    Icon(
                        imageVector = if (isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                        contentDescription = if (isDeafened) "Undeafen" else "Deafen",
                        tint = if (isDeafened) RedstoneRed else Color.White
                    )
                }

                IconButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .background(RedstoneRed.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                        .testTag("banner_disconnect_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Disconnect Voice",
                        tint = RedstoneRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProximityRadarView(
    proximityRadiusMeters: Int,
    participants: List<VoiceParticipant>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .background(StoneSurfaceVariant, RoundedCornerShape(16.dp))
            .border(1.dp, MinecraftBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2.2f

            // Proximity zones (circles)
            drawCircle(
                color = MinecraftGreen.copy(alpha = 0.15f),
                radius = maxRadius,
                center = center
            )
            drawCircle(
                color = MinecraftGreen.copy(alpha = pulseAlpha),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = DiamondCyan.copy(alpha = 0.25f),
                radius = maxRadius * 0.55f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = DiamondCyan.copy(alpha = 0.35f),
                radius = maxRadius * 0.25f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Crosshairs
            drawLine(
                color = MinecraftBorder,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = MinecraftBorder,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Self icon at center
            drawCircle(
                color = MinecraftGreen,
                radius = 8.dp.toPx(),
                center = center
            )
        }

        // Radar info overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SPATIAL PROXIMITY RADAR",
                color = MinecraftGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${proximityRadiusMeters}m Max Range",
                color = DiamondCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "YOU (CENTER)",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center).padding(top = 28.dp)
        )
    }
}

@Composable
fun VoiceParticipantCard(
    participant: VoiceParticipant,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("participant_card_${participant.username}"),
        colors = CardDefaults.cardColors(containerColor = BedrockSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (participant.isSpeaking) 2.dp else 1.dp,
            color = if (participant.isSpeaking) SpeakingHalo else MinecraftBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StoneSurfaceVariant)
                        .border(
                            width = if (participant.isSpeaking) 2.dp else 1.dp,
                            color = if (participant.isSpeaking) SpeakingHalo else MinecraftBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    AsyncImage(
                        model = participant.skinUrl,
                        contentDescription = "${participant.username} Minecraft skin",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participant.username,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${participant.minecraftUsername})",
                            color = DiamondCyan,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (participant.isSpeaking) {
                            Text(
                                text = "Speaking",
                                color = SpeakingHalo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Listening",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        if (participant.distanceMeters > 0) {
                            Text(
                                text = " • ${participant.distanceMeters}m away",
                                color = DiamondCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (participant.isMuted) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "User Muted",
                        tint = RedstoneRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // User Volume control slider
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "User volume",
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..2f,
                    colors = SliderDefaults.colors(
                        thumbColor = MinecraftGreen,
                        activeTrackColor = MinecraftGreen,
                        inactiveTrackColor = StoneSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
