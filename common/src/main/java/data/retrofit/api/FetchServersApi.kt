package data.retrofit.api

import androidx.annotation.Keep
import data.retrofit.model.server.FetchServersRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@Keep
interface FetchServersApi {
    @POST("getServers")
    @Headers("Accept-Encoding: br")
    suspend fun fetchServers(@Body request: FetchServersRequest) : Response<ResponseBody>
}