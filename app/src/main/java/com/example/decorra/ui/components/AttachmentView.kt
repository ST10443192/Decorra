package com.example.decorra.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.FileRepository
import kotlinx.coroutines.launch

/** A photo thumbnail or a PDF row. Tapping downloads the file (once) and opens it. */
@Composable
fun AttachmentView(fileId: String, name: String, mime: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val failedText = stringResource(R.string.cannot_open_file)
    var busy by remember { mutableStateOf(false) }

    val open: () -> Unit = {
        if (!busy) {
            busy = true
            scope.launch {
                runCatching { FileRepository.download(context, fileId, mime) }
                    .onSuccess { FileRepository.open(context, it, mime) }
                    .onFailure { Toast.makeText(context, failedText, Toast.LENGTH_LONG).show() }
                busy = false
            }
        }
    }

    if (mime.startsWith("image/")) {
        val bitmap by produceState<Bitmap?>(null, fileId) {
            value = runCatching { FileRepository.loadBitmap(context, fileId, mime) }.getOrNull()
        }
        Box(
            Modifier
                .size(width = 200.dp, height = 150.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = open),
            contentAlignment = Alignment.Center
        ) {
            val image = bitmap
            if (image != null) {
                Image(
                    image.asImageBitmap(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    } else {
        Row(Modifier.clickable(onClick = open), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Description, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}