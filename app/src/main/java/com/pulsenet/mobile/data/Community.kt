package com.pulsenet.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "communities")
data class Community(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val creatorPublicKey: String
)
