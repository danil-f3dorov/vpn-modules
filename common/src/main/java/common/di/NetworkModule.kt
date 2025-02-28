package common.di

import com.google.gson.GsonBuilder
import common.data.remote.api.FetchServerListApi
import common.di.annotations.Test
import dagger.Module
import dagger.Provides
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.net.InetAddress
import javax.inject.Singleton

@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideProdRetrofitClient(client: OkHttpClient): FetchServerListApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vpnduck.com/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(client)
            .build()
        return retrofit.create<FetchServerListApi>()
    }

    @Provides
    @Test
    @Singleton
    fun provideTestRetrofitClient(@Test client: OkHttpClient): FetchServerListApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://vpn-api.com/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(client)
            .build()
        return retrofit.create<FetchServerListApi>()
    }

    @Provides
    @Singleton
    fun provideProdOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Test
    @Singleton
    fun provideTestOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        localServerDns: Dns
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .dns(localServerDns)
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideTestServerDns(): Dns {
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
}