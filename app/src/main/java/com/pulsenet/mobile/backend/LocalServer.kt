package com.pulsenet.mobile.backend

import com.pulsenet.mobile.data.PostDao
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalServer(private val postDao: PostDao) {
    private var server: NettyApplicationEngine? = null

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            server = embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
                install(ContentNegotiation) {
                    json()
                }
                configureRouting(postDao)
            }.start(wait = true)
        }
    }

    fun stop() {
        server?.stop(1000, 2000)
    }
}
