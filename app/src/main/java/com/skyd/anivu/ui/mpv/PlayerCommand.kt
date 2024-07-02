package com.skyd.anivu.ui.mpv

import android.net.Uri
import androidx.compose.ui.geometry.Offset

sealed interface PlayerCommand {
    data class SetUri(val uri: Uri) : PlayerCommand
    data object Destroy : PlayerCommand
    data class Paused(val paused: Boolean) : PlayerCommand
    data object GetPaused : PlayerCommand
    data object PlayOrPause : PlayerCommand
    data class SeekTo(val position: Int) : PlayerCommand
    data class Rotate(val rotate: Int) : PlayerCommand
    data class Zoom(val zoom: Float) : PlayerCommand
    data object GetZoom : PlayerCommand
    data class VideoOffset(val offset: Offset) : PlayerCommand
    data object GetVideoOffsetX : PlayerCommand
    data object GetVideoOffsetY : PlayerCommand
    data class SetSpeed(val speed: Float) : PlayerCommand
    data object GetSpeed : PlayerCommand
    data object LoadAllTracks : PlayerCommand
    data object GetSubtitleTrack : PlayerCommand
    data class SetSubtitleTrack(val trackId: Int) : PlayerCommand
    data object GetAudioTrack : PlayerCommand
    data class SetAudioTrack(val trackId: Int) : PlayerCommand
    data object Screenshot : PlayerCommand
    data class AddSubtitle(val filePath: String) : PlayerCommand
    data class AddAudio(val filePath: String) : PlayerCommand
    data object GetBuffer : PlayerCommand
}