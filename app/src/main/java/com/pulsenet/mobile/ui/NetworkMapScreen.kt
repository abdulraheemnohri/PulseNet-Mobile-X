package com.pulsenet.mobile.ui

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NetworkMapScreen(peers: List<NsdServiceInfo>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "P2P Network Map",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (peers.isEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Scanning for nearby nodes...", style = MaterialTheme.typography.bodyMedium)
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Text(
                text = "Found ${peers.size} active nodes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(peers) { peer ->
                    PeerItem(peer)
                }
            }
        }
    }
}

@Composable
fun PeerItem(peer: NsdServiceInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = peer.serviceName, style = MaterialTheme.typography.titleSmall)
            Text(text = "Address: ${peer.host?.hostAddress ?: "Resolving..."}:${peer.port}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
