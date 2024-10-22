package common.di

import android.content.Context
import androidx.room.Room
import common.data.local.dao.ServerDao
import common.data.local.db.VpnDatabase
import common.data.local.migration.MIGRATION_1_2
import common.data.remote.api.FetchServerListApi
import common.data.remote.client.RetrofitClient
import common.data.repository.VpnRepositoryImpl
import common.domain.repository.VpnRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val dataModule = module {
    single { provideVpnDatabase(androidContext()) }
    single { provideServerDao(get()) }
    single { provideFetchServerListApi() }
    single<VpnRepository> { provideVpnRepository(get(), get()) }
}

fun provideVpnDatabase(context: Context): VpnDatabase {
    return Room.databaseBuilder(context, VpnDatabase::class.java, "vpn_duck.roomdb")
        .addMigrations(MIGRATION_1_2)
        .build()
}

fun provideServerDao(db: VpnDatabase): ServerDao {
    return db.getServerDao()
}

fun provideFetchServerListApi(): FetchServerListApi {
    return RetrofitClient.fetchServerListApi
}

fun provideVpnRepository(
    serverDao: ServerDao,
    fetchServerListApi: FetchServerListApi
): VpnRepository {
    return VpnRepositoryImpl(serverDao = serverDao, api = fetchServerListApi)
}