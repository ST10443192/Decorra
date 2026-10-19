package com.example.decorra.ui.client

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.decorra.R
import com.example.decorra.data.ChatMessage
import com.example.decorra.data.ChatRepository
import com.example.decorra.data.FileRepository
import com.example.decorra.ui.components.AttachmentView
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.theme.Sand
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.timeText
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

/**
 * One live conversation. asClient = true in the client app, false when a decorator replies.
 * threadId is the client's user id.
 */
@Composable
fun ChatScreen(
    title: String,
    threadId: String,
    asClient: Boolean,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val messages by remember(threadId) {
        if (threadId.isBlank()) emptyFlow<List<ChatMessage>>() else ChatRepository.messages(threadId)
    }.collectAsState(initial = emptyList<ChatMessage>())
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val tooLargeText = stringResource(R.string.file_too_large)
    val uploadFailedText = stringResource(R.string.upload_failed)
    var uploading by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    val upload: (Uri) -> Unit = { uri ->
        if (threadId.isNotBlank() && !uploading) {
            uploading = true
            scope.launch {
                FileRepository.upload(context, uri, threadId)
                    .onSuccess { ChatRepository.send(threadId, it.name, asClient, it) }
                    .onFailure {
                        val text =
                            if (it is FileRepository.TooLargeException) tooLargeText else uploadFailedText
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                    }
                uploading = false
            }
        }
    }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) upload(uri) }
    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) upload(uri) }

    // While this conversation is on screen, its push notifications are not shown.
    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner, threadId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> ChatRepository.openThreadId = threadId
                Lifecycle.Event.ON_PAUSE ->
                    if (ChatRepository.openThreadId == threadId) ChatRepository.openThreadId = null
                else -> Unit
            }
        }
        if (lifecycleOwner == null) ChatRepository.openThreadId = threadId
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            if (ChatRepository.openThreadId == threadId) ChatRepository.openThreadId = null
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        DecorraTopBar(
            title = title,
            navigationIcon = { if (onBack != null) BackButton(onBack) }
        )

        Box(Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                EmptyState(
                    title = R.string.chat_empty_title,
                    message = R.string.chat_empty_message
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message, mine = message.fromClient == asClient)
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(onClick = { menuOpen = true }, enabled = !uploading) {
                    if (uploading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = stringResource(R.string.attach)
                        )
                    }
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.attach_photo)) },
                        onClick = {
                            menuOpen = false
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.attach_document)) },
                        onClick = {
                            menuOpen = false
                            documentPicker.launch(FileRepository.documentTypes)
                        }
                    )
                }
            }

            OutlinedTextField(
                value = draft,
                onValueChange = { if (it.length <= 2000) draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.type_message)) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            IconButton(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty() && threadId.isNotBlank()) {
                        ChatRepository.send(threadId, text, asClient)
                        draft = ""
                    }
                },
                enabled = draft.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, mine: Boolean) {
    val shape = RoundedCornerShape(16.dp)
    val fileId = message.fileId
    Box(
        Modifier.fillMaxWidth(),
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            Modifier
                .widthIn(max = 280.dp)
                .background(if (mine) Sand else Color.White, shape)
                .then(
                    if (mine) Modifier
                    else Modifier.border(1.dp, Taupe.copy(alpha = 0.5f), shape)
                )
                .padding(12.dp)
        ) {
            if (fileId != null) {
                AttachmentView(fileId, message.fileName.orEmpty(), message.fileMime.orEmpty())
            } else {
                Text(message.text)
            }
            Text(
                timeText(message.timeMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}