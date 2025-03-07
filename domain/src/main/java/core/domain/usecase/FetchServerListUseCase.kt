package core.domain.usecase

import core.domain.repository.VpnRepository
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class FetchServerListUseCase @Inject constructor(private val repository: VpnRepository) {
    suspend operator fun invoke(version: Int): Result<Response<ResponseBody>> =
        runCatching {
            repository.fetchServerList(version).also { response ->
                if (!response.isSuccessful) throw Exception("Server error: ${response.code()}")
            }
        }.recoverCatching { throwable ->
            when (throwable) {
                is UnknownHostException -> throw Exception("Internet Error", throwable)
                is SocketTimeoutException -> throw Exception("Connection timeout", throwable)
                is IOException -> throw Exception("Input output error", throwable)
                else -> throw throwable
            }
        }
}