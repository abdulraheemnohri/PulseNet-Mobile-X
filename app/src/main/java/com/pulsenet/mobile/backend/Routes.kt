package com.pulsenet.mobile.backend

import com.pulsenet.mobile.data.PostDao
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting(postDao: PostDao) {
    routing {
        get("/") {
            call.respondText("PulseNet Node API is running local.")
        }

        get("/posts") {
            // In a real app, we'd collect from the flow or use a one-shot query.
            // For MVP, we'll just return a success message or mock data if we can't easily access DB here without flow collection.
            call.respondText("Posts endpoint reached")
        }
    }
}
