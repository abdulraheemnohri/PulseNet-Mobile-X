package com.pulsenet.mobile.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Post::class], version = 1, exportSchema = false)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
