package ireader.presentation.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.file.getAbsolutePath
import ireader.core.source.model.MovieUrl
import ireader.core.source.model.Subtitles
import ireader.domain.utils.extensions.*
import ireader.i18n.UiText
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.video.bottomsheet.audioTracksComposable
import ireader.presentation.ui.video.bottomsheet.loadLocalFileComposable
import ireader.presentation.ui.video.bottomsheet.playBackSpeedComposable
import ireader.presentation.ui.video.bottomsheet.subtitleSelectorComposable
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.core.toSubtitleData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoPlayerBottomSheet(
    playerState: PlayerState,
    mediaState: MediaState,
    controller: Controller,
    vm: VideoScreenViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        LazyColumn {
            playBackSpeedComposable(playerState) {
                playerState.playbackSpeed = it
                vm.player?.value?.setPlaybackSpeed(it)
                scope.launch {
                    controller.sheetState.hide()
                }
                vm.showSnackBar(UiText.DynamicString("Playback speed: $it."))
            }
            audioTracksComposable(playerState, mediaState) {
                mediaState?.setPreferredAudioTrack(it)
                scope.launch {
                    controller.sheetState.hide()
                }
                vm.showSnackBar(UiText.DynamicString("Audio Tracks: $it."))
            }
            subtitleSelectorComposable(playerState, mediaState.activeSubtitles.value) { subtitleData ->
                playerState.currentSubtitle = subtitleData

                mediaState.setPreferredSubtitles(subtitleData).let { result ->
                    if (result) {
                        mediaState.reloadPlayer()
                    }
                }
                scope.launch {
                    controller.sheetState.hide()
                }
                vm.showSnackBar(UiText.DynamicString("Subtitle: ${subtitleData?.name}."))
            }
            loadLocalFileComposable("Load Video From Local Storage") {
                vm.simpleStorage.simpleStorageHelper.openFilePicker(300, false, "video/*")
                vm.simpleStorage.simpleStorageHelper.onFileSelected = { requestCode, files ->
                    context.findComponentActivity()?.lifecycleScope?.launchIO {
                        val firstFile = files.first().getAbsolutePath(context)
                        vm.chapter =
                            vm.chapter?.copy(content = listOf(MovieUrl(firstFile.toString())))
                        vm.insertUseCases.insertChapter(vm.chapter)
                        vm.mediaState.subs = emptyList()
                        vm.mediaState.medias = emptyList()
                        vm.mediaState.medias = listOf(MovieUrl(firstFile))
                        withUIContext {
                            mediaState.currentLink = null
                            mediaState.currentDownloadedFile = firstFile
                            mediaState.saveData()
                            mediaState.reloadPlayer()
                        }
                        vm.showSnackBar(UiText.DynamicString("File Selected."))
                        scope.launch {
                            controller.sheetState.hide()
                        }
                    }
                }
            }
            loadLocalFileComposable("Load Subtitle from Local Storage") {
                vm.simpleStorage.simpleStorageHelper.openFilePicker(300, false, "application/*")
                vm.simpleStorage.simpleStorageHelper.onFileSelected = { requestCode, files ->
                    context.findComponentActivity()?.lifecycleScope?.launchIO {
                        val file = files.first()
                        val path = file.getAbsolutePath(context)
                        val sub = Subtitles(path).toSubtitleData()
                        val subs =  mediaState.activeSubtitles.value + listOf(sub)
                        withUIContext {
                            mediaState.saveData()
                            mediaState.setActiveSubtitles(subs)
                            mediaState.reloadPlayer()
                        }
                        vm.showSnackBar(UiText.DynamicString("Subtitle is Selected."))
                        scope.launch {
                            controller.sheetState.hide()
                        }
                    }
                }
            }
        }
    }
}