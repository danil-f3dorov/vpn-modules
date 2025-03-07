package core.domain.repository

import core.domain.model.Server
import okhttp3.ResponseBody
import retrofit2.Response


interface VpnRepository {
    suspend fun addSererList(serverList: List<Server>)
    suspend fun getServerList(): List<Server>
    suspend fun deleteServer(server: Server)
    suspend fun addServer(server: Server)
    suspend fun fetchServerList(version: Int) : Response<ResponseBody>
}
