package com.pulsenet.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.pulsenet.mobile.backend.LocalServer
import com.pulsenet.mobile.backend.PeerDiscoveryManager
import com.pulsenet.mobile.backend.SyncManager
import com.pulsenet.mobile.data.IdentityManager
import com.pulsenet.mobile.data.Post
import com.pulsenet.mobile.data.PulseDatabase
import com.pulsenet.mobile.ui.HomeFeedScreen
import com.pulsenet.mobile.ui.NetworkMapScreen
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var db: PulseDatabase
    private lateinit var server: LocalServer
    private lateinit var discoveryManager: PeerDiscoveryManager
    private lateinit var syncManager: SyncManager
    private val identityManager = IdentityManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            PulseDatabase::class.java, "pulse-db"
        ).build()

        server = LocalServer(db.postDao())
        server.start()

        discoveryManager = PeerDiscoveryManager(this)
        discoveryManager.registerService(8080)
        discoveryManager.discoverServices()

        syncManager = SyncManager(db.postDao())
        syncManager.startSync(discoveryManager.discoveredPeers)

        setContent {
            PulseNetApp(db, discoveryManager, identityManager) { content ->
                lifecycleScope.launch {
                    val signature = identityManager.sign(content)
                    val post = Post(
                        id = UUID.randomUUID().toString(),
                        author = "Me",
                        content = content,
                        timestamp = System.currentTimeMillis(),
                        publicKey = identityManager.getPublicKey(),
                        signature = signature
                    )
                    db.postDao().insertPost(post)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
        discoveryManager.stop()
        syncManager.stop()
    }
}

@Composable
fun PulseNetApp(
    db: PulseDatabase,
    discoveryManager: PeerDiscoveryManager,
    identityManager: IdentityManager,
    onCreatePost: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val posts by db.postDao().getAllPosts().collectAsState(initial = emptyList())
    val peers by discoveryManager.discoveredPeers.collectAsState()

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Share, contentDescription = "Network") },
                        label = { Text("Network") }
                    )
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> HomeFeedScreen(posts, onCreatePost)
                    1 -> NetworkMapScreen(peers)
                }
            }
        }
    }
}
