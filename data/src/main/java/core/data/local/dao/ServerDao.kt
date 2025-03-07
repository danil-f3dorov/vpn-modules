package core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import core.data.local.entity.ServerEntity

@Dao
interface ServerDao {
    @Insert
    suspend fun addServerList(serverEntityList: List<ServerEntity>)

    @Query("Select * from servers")
    suspend fun getServerList(): List<ServerEntity>

    @Delete
    suspend fun deleteServer(serverEntity: ServerEntity)

    @Insert
    suspend fun addServer(serverEntity: ServerEntity)
}