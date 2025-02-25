package common.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.R
import com.fasterxml.jackson.databind.ObjectMapper
import common.App
import common.App.Companion.duntaManager
import common.data.remote.model.FetchServerListResponse
import common.domain.model.Server
import common.domain.usecase.AddServerListUseCase
import common.domain.usecase.AddServerUseCase
import common.domain.usecase.DeleteServerUseCase
import common.domain.usecase.FetchServerListUseCase
import common.domain.usecase.GetListVersionUseCase
import common.domain.usecase.GetServerListUseCase
import common.domain.usecase.ResetListVersionUseCase
import common.domain.usecase.SetListVersionUseCase
import common.util.enum.HomeScreenState
import common.util.parse.ParseSpeed.parseSpeed
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import observers.VpnTrafficObserver
import org.brotli.dec.BrotliInputStream
import java.io.ByteArrayInputStream
import javax.inject.Inject

const val REQUEST_CODE = 919

class MainViewModel @Inject constructor(
    private val fetchServerListUseCase: FetchServerListUseCase,
    private val getServerListUseCase: GetServerListUseCase,
    private val addServerListUseCase: AddServerListUseCase,
    private val addServerUseCase: AddServerUseCase,
    private val deleteServerUseCase: DeleteServerUseCase,
    private val setListVersionUseCase: SetListVersionUseCase,
    private val getListVersionUseCase: GetListVersionUseCase,
    private val resetListVersionUseCase: ResetListVersionUseCase
) : ViewModel() {
    val screenState = mutableStateOf(HomeScreenState.Disconnected)
    private var isConnected = false
    var currentServer: Server? = null

    init {
        observeStatus()
    }

    private val _srvListStateFlow = MutableStateFlow<List<Server>?>(null)
    val srvListStateFlow get() = _srvListStateFlow

    fun updateSrvListStateFlow() {
        viewModelScope.launch(Dispatchers.Default) {
            _srvListStateFlow.value = getServerListUseCase()
        }
    }

    fun observeStatus() {
        viewModelScope.launch(Dispatchers.Default) {
            observers.VpnStatusObserver.vpnState.collect { vpnState ->
                handleVpnConnectionStatus(vpnState)
            }
        }
    }

    private suspend fun handleVpnConnectionStatus(
        vpnStatus: VpnStatus.ConnectionState,
    ) = withContext(Dispatchers.Main) {
        if (vpnStatus == VpnStatus.ConnectionState.LEVEL_CONNECTED) {
            isConnected = true
            screenState.value = HomeScreenState.Connected
        }
    }


    fun observeTraffic(
        tvDownloadSpeed: TextView, tvUploadSpeed: TextView, homeActivity: AppCompatActivity
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            VpnTrafficObserver.downloadSpeed.combine(VpnTrafficObserver.uploadSpeed) { downloadSpeed, uploadSpeed ->
                Pair(parseSpeed(downloadSpeed), parseSpeed(uploadSpeed))
            }.collect { (parsedDownloadSpeed, parsedUploadSpeed) ->
                withContext(Dispatchers.Main) {
                    tvDownloadSpeed.text = parsedDownloadSpeed
                    tvUploadSpeed.text = parsedUploadSpeed
                    notification(
                        "↑ $parsedDownloadSpeed kb/s ↓ $parsedUploadSpeed kb/s", homeActivity
                    )
                }
            }
        }
    }

    fun fetchServerList(
        noNetwork: () -> Unit,
        navHome: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO + CoroutineName("Денчик")).launch {
            val listVersion = getListVersionUseCase()
            val result = fetchServerListUseCase(listVersion)

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
                                    fetchServersResponse.server, noNetwork, navHome
                                )
                                val serverListVersion = fetchServersResponse.version?.toInt()
                                saveServerList(
                                    fetchServersResponse.server,
                                    listVersion,
                                    serverListVersion
                                )
                            }
                        }
                    } else {
                        noNetwork()
                    }
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        noNetwork()
                    }
                }
            )
        }
    }

    private suspend fun setCurrentServer(
        serverList: List<Server>,
        noNetwork: () -> Unit,
        navHome: () -> Unit
    ) {
        val nonZeroRServers = serverList.filter { (it.r ?: 0) > 0 }
        if (nonZeroRServers.isNotEmpty()) {
            currentServer = nonZeroRServers.random()
            withContext(Dispatchers.Main) {
                navHome()
            }
        } else if (serverList.isNotEmpty()) {
            currentServer = serverList.random()
            withContext(Dispatchers.Main) {
                navHome()
            }
        } else {
            val serverListDB = getServerListUseCase()
            if (serverListDB.isNotEmpty()) {
                setCurrentServer(serverListDB, noNetwork, navHome)
            } else {
                resetListVersionUseCase()
                withContext(Dispatchers.Main) {
                    noNetwork()
                }
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
            newListVersion?.let { setListVersionUseCase(it) }
            return@withContext
        }

        if (listVersion > 0) {
            val oldServerList = getServerListUseCase()
            updateServerList(newServerList, oldServerList)
            newListVersion?.let { setListVersionUseCase(it) }
            return@withContext
        }
    }

    private suspend fun updateServerList(newServerList: List<Server>, oldServerList: List<Server>) =
        withContext(Dispatchers.Default) {
            for (server in oldServerList) {
                if (!newServerList.contains(server)) {
                    deleteServerUseCase(server)
                }
            }
            for (server in newServerList) {
                if (!oldServerList.contains(server)) {
                    addServerUseCase(server)
                }
            }
        }

    private suspend fun addServerList(newServerList: List<Server>) =
        withContext(Dispatchers.Default) {
            addServerListUseCase(newServerList)
        }


    companion object {
        fun initDuntaSDK() {
            duntaManager.setPartnerId(1)
            duntaManager.setApplicationId(getApplicationId())
            duntaManager.start(App.instance)
        }

        private fun getApplicationId(): Int {
            return when (App.instance.packageName) {
                "com.vpnduck" -> 3
                "com.vpndonkey" -> 4
                "com.indianvpn" -> 4
                else -> -1
            }
        }
    }

    private fun notification(speed: String, homeActivity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("1", "notification", NotificationManager.IMPORTANCE_LOW)
            notificationChannel.setSound(null, null)
            val notificationManager = homeActivity.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        var cityName = ""
        if (currentServer?.city != "") {
            cityName = ", ${currentServer?.city}"
        }
        val countryName = "${currentServer?.country}$cityName"

        val intent = Intent(homeActivity, homeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        intent.putExtra(Server::class.java.canonicalName, currentServer!!)

        val pendingIntent = PendingIntent.getActivity(
            homeActivity,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification =
            NotificationCompat.Builder(homeActivity, "1")
                .setSmallIcon(R.drawable.sphere_small)
                .setContentTitle("VPN is connected")
                .setContentText("Connected to $countryName\n$speed")
                .setContentIntent(pendingIntent)
                .build()

        val notificationManagerCompat = NotificationManagerCompat.from(homeActivity)
        if (ActivityCompat.checkSelfPermission(
                homeActivity, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                homeActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2
            )
            return
        }
        notificationManagerCompat.notify(1, notification)
    }
}