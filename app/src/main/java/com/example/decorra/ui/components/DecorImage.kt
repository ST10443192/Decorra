package com.example.decorra.ui.components

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

private val cache = LruCache<String, ImageBitmap>(32)

private fun decode(path: String, maxSide: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0) return null
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        ?.asImageBitmap()
}

/**
 * Shows the photo at [path], or the grey placeholder while loading or when there is none.
 * [maxSidePx] keeps small thumbnails from holding full-size pictures in memory.
 */
@Composable
fun DecorImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxSidePx: Int = 800
) {
    if (path == null) {
        ImagePlaceholder(modifier)
        return
    }

    val key = "$path@$maxSidePx"
    val bitmap by produceState<ImageBitmap?>(initialValue = cache.get(key), key) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                decode(path, maxSidePx)?.also { cache.put(key, it) }
            }
        }
    }

    val loaded = bitmap
    if (loaded == null) {
        ImagePlaceholder(modifier)
    } else {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    }
}