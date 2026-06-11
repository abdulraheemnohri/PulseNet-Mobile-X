package com.pulsenet.mobile.backend

import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.pulsenet.mobile.data.Post
import com.pulsenet.mobile.data.PostDao
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class SyncManager(private val postDao: PostDao) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
    }
    private var syncJob: Job? = null

    fun startSync(peersFlow: kotlinx.coroutines.flow.StateFlow<List<NsdServiceInfo>>) {
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val peers = peersFlow.value
                for (peer in peers) {
                    try {
                        val host = peer.host?.hostAddress ?: continue
                        val port = peer.port
                        val url = "http://$host:$port/posts"

                        Log.d("Sync", "Syncing with $url")
                        val remotePosts: List<Post> = client.get(url).body()
                        val localPosts = postDao.getAllPosts().first()
                        val localIds = localPosts.map { it.id }.toSet()

                        val newPosts = remotePosts.filter { it.id !in localIds }
                        if (newPosts.isNotEmpty()) {
                            Log.d("Sync", "Found ${newPosts.size} new posts from peer")
                            postDao.insertPosts(newPosts)
                        }
                    } catch (e: Exception) {
                        Log.e("Sync", "Failed to sync with peer ${peer.serviceName}", e)
                    }
                }
                delay(10000) // Sync every 10 seconds
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        client.close()
    }
}
