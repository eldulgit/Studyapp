package com.example.studyapp.ui.settings.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ProfileImageCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropComplete: (Uri) -> Unit
) {
    val context = LocalContext.current
    val cropSize = 260.dp
    val cropSizePx = with(androidx.compose.ui.platform.LocalDensity.current) {
        cropSize.toPx()
    }

    var scale by remember(imageUri) { mutableFloatStateOf(1f) }
    var offset by remember(imageUri) { mutableStateOf(Offset.Zero) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "프로필 이미지 자르기")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(cropSize)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .pointerInput(imageUri) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(cropSize)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "손가락으로 위치를 옮기고 확대해서 맞춰주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "취소")
                }

                TextButton(
                    onClick = {
                        val croppedUri = cropProfileImageToUri(
                            context = context,
                            sourceUri = imageUri,
                            scale = scale,
                            offset = offset,
                            viewportSizePx = cropSizePx
                        )
                        onCropComplete(croppedUri)
                    }
                ) {
                    Text(text = "적용")
                }
            }
        }
    )
}

private fun cropProfileImageToUri(
    context: Context,
    sourceUri: Uri,
    scale: Float,
    offset: Offset,
    viewportSizePx: Float
): Uri {
    val bitmap = decodeBitmap(context, sourceUri)
    val cropSizeInImage = viewportSizePx / (baseCropScale(bitmap, viewportSizePx) * scale)
    val cropSize = cropSizeInImage
        .coerceAtMost(bitmap.width.toFloat())
        .coerceAtMost(bitmap.height.toFloat())

    val imageScale = baseCropScale(bitmap, viewportSizePx) * scale
    val displayedWidth = bitmap.width * imageScale
    val displayedHeight = bitmap.height * imageScale
    val imageLeft = (viewportSizePx - displayedWidth) / 2f + offset.x
    val imageTop = (viewportSizePx - displayedHeight) / 2f + offset.y

    val cropLeft = ((0f - imageLeft) / imageScale)
        .coerceIn(0f, bitmap.width - cropSize)
        .roundToInt()
    val cropTop = ((0f - imageTop) / imageScale)
        .coerceIn(0f, bitmap.height - cropSize)
        .roundToInt()
    val cropSizeInt = cropSize.roundToInt().coerceAtLeast(1)

    val cropped = Bitmap.createBitmap(
        bitmap,
        cropLeft,
        cropTop,
        cropSizeInt.coerceAtMost(bitmap.width - cropLeft),
        cropSizeInt.coerceAtMost(bitmap.height - cropTop)
    )
    val output = Bitmap.createScaledBitmap(cropped, 512, 512, true)
    val file = File(context.cacheDir, "profile_cropped_${System.currentTimeMillis()}.jpg")

    FileOutputStream(file).use { stream ->
        output.compress(Bitmap.CompressFormat.JPEG, 92, stream)
    }

    if (cropped != bitmap) cropped.recycle()
    if (output != cropped) output.recycle()
    bitmap.recycle()

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

private fun baseCropScale(bitmap: Bitmap, viewportSizePx: Float): Float {
    return max(
        viewportSizePx / bitmap.width.toFloat(),
        viewportSizePx / bitmap.height.toFloat()
    )
}

private fun decodeBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }
}
