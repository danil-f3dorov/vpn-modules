package common.viewmodel

import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import common.App
import common.util.PrefDao
import data.retrofit.client.RetrofitClient
import data.retrofit.model.server.FetchServersRequest
import data.retrofit.model.server.FetchServersResponse
import data.room.db.VpnDatabase
import data.room.entity.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.brotli.dec.BrotliInputStream
import java.io.ByteArrayInputStream

class FetchServerListViewModel : ViewModel() {

    private val serverApi = RetrofitClient.fetchServersApi
    private val serverDao = VpnDatabase.getDataBase(App.instance).getServerDao()
    private val prefDao = PrefDao(App.instance)


    fun fetchServerList(retry: () -> Unit, navigateToActivity: (server: Server) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listVersion = prefDao.getVersion()
                val response = serverApi.fetchServers(FetchServersRequest(listVersion))

                if (response.isSuccessful) {
                    response.body()?.byteStream()?.use { byteStream ->
                        BrotliInputStream(byteStream).use { brotliInputStream ->
                            val byteData = brotliInputStream.readBytes()
                            val inputStream = ByteArrayInputStream(byteData)
                            val objectMapper = ObjectMapper()
                            val fetchServersResponse = objectMapper.readValue(
                                inputStream, FetchServersResponse::class.java
                            )
                            setCurrentServer(
                                fetchServersResponse.servers, retry, navigateToActivity
                            )
                            val serverListVersion = fetchServersResponse.version?.toInt()
                            saveServerList(
                                fetchServersResponse.servers, listVersion, serverListVersion
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { retry() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { retry() }
            }

        }
    }

    private suspend fun setCurrentServer(
        serverList: List<Server>, retry: () -> Unit, navigateToActivity: (server: Server) -> Unit
    ) {
        val nonZeroRServers = serverList.filter { (it.r ?: 0) > 0 }
        if (nonZeroRServers.isNotEmpty()) {
            val randomServer = nonZeroRServers.random()
            navigateToActivity(randomServer)
            return
        } else if (serverList.isNotEmpty()) {
            val randomServer = serverList.random()
            navigateToActivity(randomServer)
            return
        } else {
            val serverListDB = serverDao.getServerList()
            if (serverListDB.isNotEmpty()) {
                setCurrentServer(serverListDB, retry, navigateToActivity)
            } else {
                retry()
            }
        }

    }

    private suspend fun saveServerList(
        newServerList: List<Server>, listVersion: Int, newListVersion: Int?
    ) = withContext(Dispatchers.Default) {

        if (newServerList.isEmpty()) {
            return@withContext
        }

        if (listVersion == 0) {
            addServerList(newServerList)
            prefDao.updateVersion(newListVersion)
            return@withContext
        }

        if (listVersion > 0) {
            val oldServerList = serverDao.getServerList()
            updateServerList(newServerList, oldServerList)
            prefDao.updateVersion(newListVersion)
            return@withContext
        }
    }

    private suspend fun updateServerList(newServerList: List<Server>, oldServerList: List<Server>) =
        withContext(Dispatchers.Default) {
            for (server in oldServerList) {
                if (!newServerList.contains(server)) {
                    serverDao.deleteServer(server)
                }
            }
            for (server in newServerList) {
                if (!oldServerList.contains(server)) {
                    serverDao.addServer(server)
                }
            }
        }

    private suspend fun addServerList(newServerList: List<Server>) =
        withContext(Dispatchers.Default) {
            serverDao.addServerList(newServerList)
        }
}