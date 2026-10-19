package com.example.decorra.ui.client

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.ImageStore
import com.example.decorra.data.InspirationRepository
import com.example.decorra.data.UserSession
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorImage
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.PrimaryButton
import kotlinx.coroutines.launch

@Composable
fun InspirationScreen(onBack: () -> Unit) {
    val all by InspirationRepository.photos.collectAsState()
    val mine = all.filter { it.clientEmail.equals(UserSession.email, ignoreCase = true) }

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
                    InspirationRepository.add(UserSession.email, saved)
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.profile_my_inspiration),
            navigationIcon = { BackButton(onBack) }
        )

        Box(Modifier.weight(1f)) {
            if (mine.isEmpty()) {
                EmptyState(
                    title = R.string.inspiration_empty_title,
                    message = R.string.inspiration_empty_message
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mine, key = { it.id }) { photo ->
                        Box {
                            DecorImage(
                                path = photo.path,
                                contentDescription = stringResource(R.string.inspiration_photo),
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                            )
                            IconButton(
                                onClick = { InspirationRepository.remove(photo.id) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.remove_inspiration)
                                )
                            }
                        }
                    }
                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.add_inspiration),
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}