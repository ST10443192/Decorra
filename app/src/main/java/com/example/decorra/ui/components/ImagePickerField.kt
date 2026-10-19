package com.example.decorra.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.ImageStore
import kotlinx.coroutines.launch

/**
 * Lets the decorator pick, replace or remove one photo. Reports the saved file path.
 * [keepPath] is a photo that must not be deleted yet (the original one while editing).
 */
@Composable
fun ImagePickerField(
    imagePath: String?,
    onImagePicked: (String?) -> Unit,
    keepPath: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val failedMessage = stringResource(R.string.image_failed)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = ImageStore.import(context, uri)
                if (saved == null) {
                    Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    if (imagePath != keepPath) ImageStore.delete(imagePath)
                    onImagePicked(saved)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.image_label), style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            DecorImage(imagePath, null, Modifier.size(72.dp), maxSidePx = 300)
            Spacer(Modifier.width(12.dp))
            Column {
                TextButton(onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(stringResource(if (imagePath == null) R.string.choose_image else R.string.change_image))
                }
                if (imagePath != null) {
                    TextButton(onClick = {
                        if (imagePath != keepPath) ImageStore.delete(imagePath)
                        onImagePicked(null)
                    }) { Text(stringResource(R.string.remove_image)) }
                }
            }
        }
    }
}