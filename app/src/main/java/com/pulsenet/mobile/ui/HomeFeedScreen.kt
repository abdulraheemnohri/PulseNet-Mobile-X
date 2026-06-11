package com.pulsenet.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulsenet.mobile.data.Post

@Composable
fun HomeFeedScreen(
    posts: List<Post>,
    onCreatePost: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(posts) { post ->
                PostItem(post)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create Post") },
            text = {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Share to the swarm...") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (text.isNotBlank()) {
                        onCreatePost(text)
                        text = ""
                        showDialog = false
                    }
                }) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = post.author, style = MaterialTheme.typography.titleSmall)
                if (post.signature != null) {
                    Text(text = "✓ Verified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (post.communityId != null) {
                Text(text = "in Group: ${post.communityId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.id, style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}
