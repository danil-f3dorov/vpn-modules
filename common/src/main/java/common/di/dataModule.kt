package common.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import common.data.local.dao.ServerDao
import common.data.local.db.VpnDatabase
import common.data.local.migration.MIGRATION_1_2
import common.data.remote.api.FetchServerListApi
import common.data.repository.VpnRepositoryImpl
import common.domain.repository.VpnRepository
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress


val dataModule = module {
    single { providesVpnDatabase(androidContext()) }
    single { providesServerDao(get()) }
    single { providesFetchServerListApi(get()) }
    single<VpnRepository> { providesVpnRepository(get(), get()) }
    single { providesHttpLoggingInterceptor() }
    single { providesOkHttpClient(get()) }
    single { providesRetrofitClient(get()) }
}

fun providesVpnDatabase(context: Context): VpnDatabase {
    return Room.databaseBuilder(context, VpnDatabase::class.java, "vpn_duck.roomdb")
        .addMigrations(MIGRATION_1_2)
        .build()
}

fun providesServerDao(db: VpnDatabase): ServerDao {
    return db.getServerDao()
}

fun providesFetchServerListApi(retrofit: Retrofit): FetchServerListApi {
    return retrofit.create(FetchServerListApi::class.java)
}

fun providesRetrofitClient(client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
//        .baseUrl("http://vpn-api.com/")
        .baseUrl("https://api.vpnduck.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .client(client)
        .build()
}

fun providesOkHttpClient(
    httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .build()
}


fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}


fun providesVpnRepository(
    serverDao: ServerDao,
    fetchServerListApi: FetchServerListApi
): VpnRepository {
    return VpnRepositoryImpl(serverDao = serverDao, api = fetchServerListApi)
}

fun providesTestServerDns(): Dns {
    return object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            if (hostname == "vpn-api.com") {
                return listOf(
                    InetAddress.getByAddress(
                        "vpn-api.com", byteArrayOf(
                            192.toByte(),
                            168.toByte(), 46, 214.toByte()
                        )
                    )
                )
            }
            return Dns.SYSTEM.lookup(hostname)
        }
    }
}
