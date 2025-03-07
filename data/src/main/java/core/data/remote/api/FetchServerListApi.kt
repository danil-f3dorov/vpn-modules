package core.data.remote.api

import core.data.remote.model.FetchServerListRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface FetchServerListApi {
    @POST("getServers")
    @Headers("Accept-Encoding: br")
    suspend fun fetchServerList(@Body request: FetchServerListRequest) : Response<ResponseBody>
}