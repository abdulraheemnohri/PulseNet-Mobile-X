package com.pulsenet.mobile.backend

import com.pulsenet.mobile.data.PostDao
import com.pulsenet.mobile.data.CommunityDao
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first

fun Application.configureRouting(postDao: PostDao, communityDao: CommunityDao) {
    routing {
        get("/") {
            call.respondText("PulseNet Node API is running local.")
        }

        get("/posts") {
            val posts = postDao.getAllPosts().first()
            call.respond(posts)
        }

        get("/communities") {
            val communities = communityDao.getAllCommunities().first()
            call.respond(communities)
        }

        get("/communities/{id}/posts") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val posts = postDao.getPostsByCommunity(id).first()
            call.respond(posts)
        }

        get("/sync/hashes") {
            val posts = postDao.getAllPosts().first()
            val hashes = posts.map { it.id }
            call.respond(hashes)
        }
    }
}
