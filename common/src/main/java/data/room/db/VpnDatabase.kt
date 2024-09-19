package data.room.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.room.dao.ServerDao
import data.room.entity.Server
import data.room.entity.User


@Database(
    entities = [User::class, Server::class],
    version = 1,
    exportSchema = false
)
abstract class VpnDatabase : RoomDatabase() {

    abstract fun getServerDao(): ServerDao

    companion object {
        private var instance: VpnDatabase? = null

        fun getDataBase(context: Context): VpnDatabase {
            return instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context): VpnDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VpnDatabase::class.java,
                "vpn_duck.roomdb"
            ).build()
        }
    }
}
