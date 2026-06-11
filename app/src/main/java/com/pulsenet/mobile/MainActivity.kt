package com.pulsenet.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.pulsenet.mobile.backend.LocalServer
import com.pulsenet.mobile.backend.PeerDiscoveryManager
import com.pulsenet.mobile.backend.SyncManager
import com.pulsenet.mobile.backend.WifiDirectManager
import com.pulsenet.mobile.data.Community
import com.pulsenet.mobile.data.IdentityManager
import com.pulsenet.mobile.data.Post
import com.pulsenet.mobile.data.PulseDatabase
import com.pulsenet.mobile.engine.ConnectivityOrchestrator
import com.pulsenet.mobile.engine.NetworkMode
import com.pulsenet.mobile.ui.CommunitiesScreen
import com.pulsenet.mobile.ui.HomeFeedScreen
import com.pulsenet.mobile.ui.NetworkMapScreen
import com.pulsenet.mobile.util.HashUtils
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var db: PulseDatabase
    private lateinit var server: LocalServer
    private lateinit var discoveryManager: PeerDiscoveryManager
    private lateinit var wifiDirectManager: WifiDirectManager
    private lateinit var syncManager: SyncManager
    private val connectivityOrchestrator = ConnectivityOrchestrator()
    private val identityManager = IdentityManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            PulseDatabase::class.java, "pulse-db"
        ).build()

        server = LocalServer(db.postDao(), db.communityDao())
        server.start()

        discoveryManager = PeerDiscoveryManager(this)
        discoveryManager.registerService(8080)
        discoveryManager.discoverServices()

        wifiDirectManager = WifiDirectManager(this)
        wifiDirectManager.discoverPeers()

        syncManager = SyncManager(db.postDao(), db.communityDao())
        syncManager.startSync(discoveryManager.discoveredPeers)

        lifecycleScope.launch {
            discoveryManager.discoveredPeers.collect { peers ->
                connectivityOrchestrator.updateStatus(peers.isNotEmpty(), false)
            }
        }

        setContent {
            PulseNetApp(db, discoveryManager, wifiDirectManager, connectivityOrchestrator, identityManager)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
        discoveryManager.stop()
        syncManager.stop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseNetApp(
    db: PulseDatabase,
    discoveryManager: PeerDiscoveryManager,
    wifiDirectManager: WifiDirectManager,
    connectivityOrchestrator: ConnectivityOrchestrator,
    identityManager: IdentityManager
) {
    var selectedTab by remember { mutableStateOf(0) }
    val posts by db.postDao().getAllPosts().collectAsState(initial = emptyList())
    val communities by db.communityDao().getAllCommunities().collectAsState(initial = emptyList())
    val peers by discoveryManager.discoveredPeers.collectAsState()
    val currentMode by connectivityOrchestrator.currentMode.collectAsState()
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("NexusWave", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        NetworkModeIndicator(currentMode)
                    }
                )
            },
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
                        icon = { Icon(Icons.Default.Person, contentDescription = "Communities") },
                        label = { Text("Groups") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Share, contentDescription = "Network") },
                        label = { Text("Network") }
                    )
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> HomeFeedScreen(posts) { content ->
                        scope.launch {
                            val timestamp = System.currentTimeMillis()
                            val author = "Me"
                            val signature = identityManager.sign(content)
                            val post = Post(
                                id = HashUtils.generateCID(content, author, timestamp),
                                author = author,
                                content = content,
                                timestamp = timestamp,
                                publicKey = identityManager.getPublicKey(),
                                signature = signature
                            )
                            db.postDao().insertPost(post)
                        }
                    }
                    1 -> CommunitiesScreen(communities) { name, desc ->
                        scope.launch {
                            val community = Community(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                description = desc,
                                creatorPublicKey = identityManager.getPublicKey()
                            )
                            db.communityDao().insertCommunity(community)
                        }
                    }
                    2 -> NetworkMapScreen(peers)
                }
            }
        }
    }
}

@Composable
fun NetworkModeIndicator(mode: NetworkMode) {
    val color = when (mode) {
        NetworkMode.OFFLINE -> androidx.compose.ui.graphics.Color.Gray
        NetworkMode.LOCAL_MESH -> androidx.compose.ui.graphics.Color.Cyan
        NetworkMode.INTERNET_RELAY -> androidx.compose.ui.graphics.Color.Green
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = mode.name, style = MaterialTheme.typography.labelSmall)
    }
}
