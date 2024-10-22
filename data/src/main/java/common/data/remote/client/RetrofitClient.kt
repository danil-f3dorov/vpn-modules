package common.data.remote.client


import com.google.gson.GsonBuilder
import common.data.remote.api.FetchServerListApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    private val client = OkHttpClient.Builder()
//        .dns(object : Dns {
//            override fun lookup(hostname: String): List<InetAddress> {
//                if (hostname == "vpn-api.com") {
//                    return listOf(
//                        InetAddress.getByAddress("vpn-api.com", byteArrayOf(192.toByte(),
//                            168.toByte(), 46, 214.toByte()
//                        )))
//                }
//                return Dns.SYSTEM.lookup(hostname)
//            }
//        })
        .addInterceptor(httpLoggingInterceptor)
        .build()

    private val ApiClient = Retrofit.Builder()
//        .baseUrl("http://vpn-api.com/")
        .baseUrl("https://api.vpnduck.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .client(client)
        .build()

    var fetchServerListApi: FetchServerListApi = ApiClient.create(FetchServerListApi::class.java)
}





