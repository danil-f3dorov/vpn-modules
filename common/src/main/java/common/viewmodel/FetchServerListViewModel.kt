package common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import common.App
import common.data.remote.model.FetchServerListResponse
import common.domain.model.Server
import common.domain.usecase.AddServerListUseCase
import common.domain.usecase.AddServerUseCase
import common.domain.usecase.DeleteServerUseCase
import common.domain.usecase.FetchServerListUseCase
import common.domain.usecase.GetServerListUseCase
import common.util.PrefDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.brotli.dec.BrotliInputStream
import org.koin.java.KoinJavaComponent.inject
import java.io.ByteArrayInputStream

class FetchServerListViewModel(
) : ViewModel() {
    private val prefDao = PrefDao(App.instance)
    private val fetchServerListUseCase: FetchServerListUseCase by inject(FetchServerListUseCase::class.java)
    private val getServerListUseCase: GetServerListUseCase by inject(GetServerListUseCase::class.java)
    private val addServerListUseCase: AddServerListUseCase by inject(AddServerListUseCase::class.java)
    private val addServerUseCase: AddServerUseCase by inject(AddServerUseCase::class.java)
    private val deleteServerUseCase: DeleteServerUseCase by inject(DeleteServerUseCase::class.java)

    fun fetchServerList(retry: () -> Unit, navigateToActivity: (server: Server) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listVersion = prefDao.getVersion()
                val response = fetchServerListUseCase.execute(listVersion)
                Log.i("response", response.isSuccessful.toString())

                if (response.isSuccessful) {
                    response.body()?.byteStream()?.use { byteStream ->
                        BrotliInputStream(byteStream).use { brotliInputStream ->
                            val byteData = brotliInputStream.readBytes()
                            val inputStream = ByteArrayInputStream(byteData)
                            val objectMapper = ObjectMapper()
                            val fetchServersResponse = objectMapper.readValue(
                                inputStream, FetchServerListResponse::class.java
                            )
                            setCurrentServer(
                                fetchServersResponse.server, retry, navigateToActivity
                            )
                            val serverListVersion = fetchServersResponse.version?.toInt()
                            saveServerList(
                                fetchServersResponse.server, listVersion, serverListVersion
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { retry() }
                }
            } catch (e: Exception) {
                Log.i("response", e.message.toString())
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
            val serverListDB = getServerListUseCase.execute()
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
            val oldServerList = getServerListUseCase.execute()
            updateServerList(newServerList, oldServerList)
            prefDao.updateVersion(newListVersion)
            return@withContext
        }
    }

    private suspend fun updateServerList(newServerList: List<Server>, oldServerList: List<Server>) =
        withContext(Dispatchers.Default) {
            for (server in oldServerList) {
                if (!newServerList.contains(server)) {
                    deleteServerUseCase.execute(server)
                }
            }
            for (server in newServerList) {
                if (!oldServerList.contains(server)) {
                    addServerUseCase.execute(server)
                }
            }
        }

    private suspend fun addServerList(newServerList: List<Server>) =
        withContext(Dispatchers.Default) {
            addServerListUseCase.execute(newServerList)
        }
}