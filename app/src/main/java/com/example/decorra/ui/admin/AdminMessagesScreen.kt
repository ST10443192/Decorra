package com.example.decorra.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.ChatRepository
import com.example.decorra.data.ChatThread
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.theme.Sand
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.timeText

@Composable
fun AdminMessagesScreen(onOpen: (ChatThread) -> Unit) {
    val threads by remember { ChatRepository.threads() }
        .collectAsState(initial = emptyList<ChatThread>())

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(title = stringResource(R.string.messages_title))

        if (threads.isEmpty()) {
            EmptyState(
                title = R.string.messages_empty_title,
                message = R.string.messages_empty_message
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(threads, key = { it.clientUid }) { thread ->
                    ThreadCard(thread) { onOpen(thread) }
                }
            }
        }
    }
}

@Composable
private fun ThreadCard(thread: ChatThread, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Taupe.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    thread.clientName.ifBlank { thread.clientEmail },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(timeText(thread.lastTimeMillis), style = MaterialTheme.typography.labelMedium)
            }
            Text(
                thread.lastText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (thread.lastFromClient) {
                Text(
                    stringResource(R.string.tile_awaiting_reply),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(Sand.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}