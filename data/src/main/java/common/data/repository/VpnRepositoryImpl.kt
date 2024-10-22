package common.data.repository

import common.data.local.dao.ServerDao
import common.data.mapper.toEntity
import common.data.mapper.toModel
import common.data.remote.api.FetchServerListApi
import common.data.remote.model.FetchServerListRequest
import common.domain.model.Server
import common.domain.repository.VpnRepository
import okhttp3.ResponseBody
import retrofit2.Response

class VpnRepositoryImpl(private val serverDao: ServerDao, private val api: FetchServerListApi) : VpnRepository {

    override suspend fun addSererList(serverList: List<Server>) {
        serverDao.addServerList(serverList.map { it.toEntity() })
    }

    override suspend fun getServerList(): List<Server> {
        return serverDao.getServerList().map { it.toModel() }
    }

    override suspend fun deleteServer(server: Server) {
        serverDao.deleteServer(server.toEntity())
    }

    override suspend fun addServer(server: Server) {
        serverDao.addServer(server.toEntity())
    }

    override suspend fun fetchServerList(version: Int): Response<ResponseBody> {
        return api.fetchServerList(FetchServerListRequest(version))
    }
}