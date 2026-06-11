package com.pulsenet.mobile.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Post::class, Community::class], version = 1, exportSchema = false)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun communityDao(): CommunityDao
}
