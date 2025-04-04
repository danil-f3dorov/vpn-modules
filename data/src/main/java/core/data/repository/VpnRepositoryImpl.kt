package core.data.repository

import core.data.local.dao.ServerDao
import core.data.mapper.toEntity
import core.data.mapper.toModel
import core.data.remote.api.FetchServerListApi
import core.data.remote.model.FetchServerListRequest
import core.domain.model.Server
import core.domain.repository.VpnRepository
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class VpnRepositoryImpl @Inject constructor(
    private val serverDao: ServerDao,
    private val api: FetchServerListApi
) : VpnRepository {

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