package com.pulsenet.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val author: String,
    val content: String,
    val timestamp: Long,
    val type: String = "text",
    val publicKey: String? = null,
    val signature: String? = null,
    val communityId: String? = null
)
