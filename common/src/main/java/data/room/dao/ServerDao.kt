package data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import data.room.entity.Server

@Dao
interface ServerDao {

    @Insert
    suspend fun addServerList(serverList: List<Server>)

    @Query("Select * from servers")
    suspend fun getServerList(): List<Server>

    @Delete
    suspend fun deleteServer(server: Server)

    @Insert
    suspend fun addServer(server: Server)

}