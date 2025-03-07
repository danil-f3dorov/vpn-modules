package core.di

import android.content.Context
import androidx.room.Room
import core.data.local.dao.ServerDao
import core.data.local.db.VpnDatabase
import core.data.local.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideServerDao(db: VpnDatabase): ServerDao {
        return db.getServerDao()
    }

    @Provides
    @Singleton
    fun provideVpnDatabase(context: Context): VpnDatabase {
        return Room.databaseBuilder(context, VpnDatabase::class.java, "vpn_duck.roomdb")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}