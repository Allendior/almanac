package io.github.allendior.almanac.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.allendior.almanac.data.FramingGuide
import io.github.allendior.almanac.domain.CameraFacing
import io.github.allendior.almanac.ui.Fmt
import io.github.allendior.almanac.ui.components.ButtonTone
import io.github.allendior.almanac.ui.components.ClassicalButton
import io.github.allendior.almanac.ui.components.SelectChip
import io.github.allendior.almanac.ui.components.accessibleClick
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type
import java.nio.ByteBuffer
import java.time.LocalDate

/**
 * The capture screen inverts the ground to near-black so the viewfinder is the only
 * lit thing on the page.
 *
 * The guides are drawn by Compose on top of the preview surface. They are not part of
 * the camera pipeline, they are not composited into the image, and the bytes handed to
 * the review screen are exactly the bytes the camera produced.
 */
@Composable
fun CaptureScreen(
    guide: FramingGuide,
    today: LocalDate,
    onGuideChange: (FramingGuide) -> Unit,
    onCaptured: (ByteArray, CameraFacing) -> Unit,
    onFailed: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var facing by remember { mutableStateOf(CameraFacing.FRONT) }
    var capturing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted = it }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.darkBg)
            .padding(top = 26.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(width = 80.dp, height = 48.dp)
                    .accessibleClick(onClick = onCancel, label = "Cancel"),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("Cancel", style = Type.body14, color = Ink.darkText)
            }
            Text(
                Fmt.iso(today),
                style = Type.record(12f),
                color = Ink.darkText.copy(alpha = 0.6f),
            )
        }

        Box(Modifier.height(Space.s3))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .aspectRatio(3f / 4f)
                .border(1.dp, Ink.darkText.copy(alpha = 0.18f)),
        ) {
            if (granted) {
                CameraPreview(
                    facing = facing,
                    onImageCaptureReady = { capture -> imageCapture = capture },
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), Alignment.Center) {
                    Text(
                        "Almanac needs the camera to take a portrait.\nNothing else is requested.",
                        style = Type.body125.copy(textAlign = TextAlign.Center),
                        color = Ink.darkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(Space.s4),
                    )
                }
            }
            when (guide) {
                FramingGuide.GHOST -> GhostGuide(Modifier.fillMaxSize())
                FramingGuide.GRID -> GridGuide(Modifier.fillMaxSize())
                FramingGuide.OFF -> Unit
            }
        }

        Box(Modifier.height(Space.s3))

        Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            FramingGuide.entries.forEach { option ->
                SelectChip(
                    label = option.label,
                    selected = guide == option,
                    onClick = { onGuideChange(option) },
                    height = 40.dp,
                    accentOnDark = true,
                )
            }
        }

        Box(Modifier.height(Space.s3))

        Text(
            "Guides are drawn on screen only. The saved file is untouched — no filter, " +
                "no beautifying, no crop.",
            style = Type.body115.copy(textAlign = TextAlign.Center),
            color = Ink.darkText.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 28.dp),
        )

        Box(Modifier.weight(1f))

        // Deliberately not a compliment about appearance — this is a truthful record,
        // not a beauty app. Just a quiet nudge that there's no rush.
        Text(
            "Whenever you're ready.",
            style = Type.body125.copy(textAlign = TextAlign.Center),
            color = Ink.darkText.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.height(Space.s2))

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Deliberately empty: there is no import affordance on this screen.
            Box(Modifier.size(56.dp))

            Shutter(
                enabled = granted && !capturing,
                onClick = {
                    val capture = imageCapture
                    if (capture != null) {
                        capturing = true
                        capture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bytes = image.toJpegBytes()
                                    image.close()
                                    capturing = false
                                    onCaptured(bytes, facing)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    capturing = false
                                    onFailed(exception.message ?: "The camera reported an error.")
                                }
                            },
                        )
                    }
                },
            )

            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                ClassicalButton(
                    label = "Flip",
                    onClick = {
                        facing = if (facing == CameraFacing.FRONT) CameraFacing.REAR else CameraFacing.FRONT
                    },
                    tone = ButtonTone.Ghost,
                    height = 48.dp,
                    contentDescription = "Switch camera",
                    labelColorOverride = Ink.darkText.copy(alpha = 0.8f),
                )
            }
        }
    }
}

/** Held outside composition so the shutter always talks to the bound use case. */
private var imageCapture: ImageCapture? = null

@Composable
private fun CameraPreview(facing: CameraFacing, onImageCaptureReady: (ImageCapture) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    DisposableEffect(facing) {
        var bound: ProcessCameraProvider? = null
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = providerFuture.get()
            bound = provider
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val capture = ImageCapture.Builder()
                // Archival quality. No CameraX Extensions are used anywhere in this app,
                // so no HDR, bokeh, "face retouch" or beauty pipeline can be engaged.
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            val selector = if (facing == CameraFacing.FRONT) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                imageCapture = capture
                onImageCaptureReady(capture)
            }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { bound?.unbindAll() }
            imageCapture = null
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
    )
}

/**
 * The JPEG exactly as the camera produced it. The buffer is copied, not decoded:
 * nothing here re-encodes, rotates, mirrors or grades the image.
 */
private fun ImageProxy.toJpegBytes(): ByteArray {
    val buffer: ByteBuffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}

@Composable
private fun Shutter(enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(74.dp)
            .clip(CircleShape)
            .border(1.dp, Ink.darkText.copy(alpha = if (enabled) 0.55f else 0.25f), CircleShape)
            .accessibleClick(onClick = onClick, enabled = enabled)
            .semantics { contentDescription = "Capture portrait" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Ink.darkText.copy(alpha = if (enabled) 1f else 0.4f)),
        )
    }
}

/** Head-and-shoulders ghost, in the handoff's 24x32 space, at half opacity gold. */
@Composable
private fun GhostGuide(modifier: Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val sx = size.width / 24f
        val sy = size.height / 32f
        val stroke = Stroke(width = 0.35f * sx, cap = StrokeCap.Round)

        val headCenter = androidx.compose.ui.geometry.Offset(12f * sx, 11f * sy)
        drawOval(
            color = Ink.guideGold.copy(alpha = 0.5f),
            topLeft = androidx.compose.ui.geometry.Offset(headCenter.x - 4.6f * sx, headCenter.y - 6f * sy),
            size = androidx.compose.ui.geometry.Size(9.2f * sx, 12f * sy),
            style = stroke,
        )
        val shoulders = androidx.compose.ui.graphics.Path().apply {
            moveTo(3f * sx, 31f * sy)
            cubicTo(5f * sx, 22f * sy, 9f * sx, 19.5f * sy, 12f * sx, 19.5f * sy)
            cubicTo(15f * sx, 19.5f * sy, 19f * sx, 22f * sy, 21f * sx, 31f * sy)
        }
        drawPath(shoulders, color = Ink.guideGold.copy(alpha = 0.5f), style = stroke)
    }
}

/** Rule of thirds, in the page's own light at low opacity. */
@Composable
private fun GridGuide(modifier: Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val color = Ink.darkText.copy(alpha = 0.28f)
        val w = size.width
        val h = size.height
        for (i in 1..2) {
            val x = w * i / 3f
            drawLine(color, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, h), 1f)
            val y = h * i / 3f
            drawLine(color, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1f)
        }
    }
}
