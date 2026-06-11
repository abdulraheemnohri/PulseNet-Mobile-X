package com.pulsenet.mobile.backend

import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.pulsenet.mobile.data.Post
import com.pulsenet.mobile.data.PostDao
import com.pulsenet.mobile.data.Community
import com.pulsenet.mobile.data.CommunityDao
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class SyncManager(private val postDao: PostDao, private val communityDao: CommunityDao) {
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

                        // Sync Communities
                        val communitiesUrl = "http://$host:$port/communities"
                        val remoteCommunities: List<Community> = client.get(communitiesUrl).body()
                        val localCommunities = communityDao.getAllCommunities().first()
                        val localCommunityIds = localCommunities.map { it.id }.toSet()
                        val newCommunities = remoteCommunities.filter { it.id !in localCommunityIds }
                        if (newCommunities.isNotEmpty()) {
                            communityDao.insertCommunities(newCommunities)
                        }

                        // Sync Posts
                        val postsUrl = "http://$host:$port/posts"
                        val remotePosts: List<Post> = client.get(postsUrl).body()
                        val localPosts = postDao.getAllPosts().first()
                        val localPostIds = localPosts.map { it.id }.toSet()
                        val newPosts = remotePosts.filter { it.id !in localPostIds }
                        if (newPosts.isNotEmpty()) {
                            postDao.insertPosts(newPosts)
                        }
                    } catch (e: Exception) {
                        Log.e("Sync", "Failed to sync with peer ${peer.serviceName}", e)
                    }
                }
                delay(15000) // Sync every 15 seconds
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        client.close()
    }
}
