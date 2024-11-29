package common.viewmodel

import android.content.Context
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.brotli.dec.BrotliInputStream
import java.io.ByteArrayInputStream

class FetchServerListViewModel(
    private val fetchServerListUseCase: FetchServerListUseCase,
    private val getServerListUseCase: GetServerListUseCase,
    private val addServerListUseCase: AddServerListUseCase,
    private val addServerUseCase: AddServerUseCase,
    private val deleteServerUseCase: DeleteServerUseCase
) : ViewModel() {

    private val prefDao = PrefDao(App.instance)

    fun fetchServerList(context: Context, retry: () -> Unit, navigateToActivity: (server: Server) -> Unit) {
        CoroutineScope(Dispatchers.IO + CoroutineName("Денчик")).launch {
            val listVersion = prefDao.getVersion()
            val result = fetchServerListUseCase.execute(listVersion)

            result.fold(
                onSuccess = { response ->
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
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Server error: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            retry()
                        }
                    }
                },
                onFailure = { throwable ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            throwable.message ?: "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                        retry()
                    }
                }
            )
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