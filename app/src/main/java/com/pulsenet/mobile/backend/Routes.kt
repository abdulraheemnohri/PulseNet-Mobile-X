package com.pulsenet.mobile.backend

import com.pulsenet.mobile.data.PostDao
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first

fun Application.configureRouting(postDao: PostDao) {
    routing {
        get("/") {
            call.respondText("PulseNet Node API is running local.")
        }

        get("/posts") {
            val posts = postDao.getAllPosts().first()
            call.respond(posts)
        }

        get("/sync/hashes") {
            val posts = postDao.getAllPosts().first()
            val hashes = posts.map { it.id }
            call.respond(hashes)
        }
    }
}
