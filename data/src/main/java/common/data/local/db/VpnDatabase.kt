package common.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import common.data.local.dao.ServerDao
import common.data.local.entity.ServerEntity


@Database(
    entities = [ServerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VpnDatabase : RoomDatabase() {

    abstract fun getServerDao(): ServerDao
}
