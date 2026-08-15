package com.almanac.portrait.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.almanac.portrait.ui.theme.Ink
import java.io.File

/**
 * A portrait on screen. Two rules hold everywhere it is used:
 *
 *  - the image is drawn as the file has it — no grade, no beautification, no crop;
 *  - if the file is gone, the frame says so rather than showing a blank, because a
 *    missing original is a fact the owner needs to know about.
 *
 * A full-resolution original can take a moment to decode, especially the first time a
 * screen composes. When [thumbnailFile] is supplied, it fills that gap — the small
 * derived image the archive already has on hand — instead of an empty mat. The
 * thumbnail is never the record; it is only ever a stand-in for the instant before the
 * real file finishes decoding.
 */
@Composable
fun PortraitImage(
    file: File?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderMark: Dp = 40.dp,
    thumbnailFile: File? = null,
) {
    val exists = file != null && file.exists()
    val context = LocalContext.current
    Box(modifier.background(Ink.textGhost), contentAlignment = Alignment.Center) {
        if (!exists) {
            androidx.compose.material3.Icon(
                Lucide.PortraitMark,
                contentDescription = contentDescription,
                tint = Ink.text.copy(alpha = 0.22f),
                modifier = Modifier.size(placeholderMark),
            )
        } else if (thumbnailFile != null && thumbnailFile.exists()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(file).crossfade(false).build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(thumbnailFile).crossfade(false).build(),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    SubcomposeAsyncImageContent()
                }
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context).data(file).crossfade(false).build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** The empty 3:4 frame on Today, before the day has a portrait. */
@Composable
fun EmptyFrame(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(Ink.textGhost)
            .border(1.dp, Ink.divider),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Icon(
                Lucide.PortraitMark,
                contentDescription = null,
                tint = Ink.text.copy(alpha = 0.22f),
                modifier = Modifier.size(40.dp),
            )
            Box(Modifier.size(12.dp))
            androidx.compose.material3.Text(
                label,
                style = com.almanac.portrait.ui.theme.Type.title19,
                color = Ink.textMuted,
            )
        }
    }
}
