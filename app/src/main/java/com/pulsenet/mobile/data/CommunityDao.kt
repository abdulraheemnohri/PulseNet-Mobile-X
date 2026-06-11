package com.pulsenet.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Query("SELECT * FROM communities")
    fun getAllCommunities(): Flow<List<Community>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(community: Community)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunities(communities: List<Community>)
}
