package common.domain.usecase

import common.domain.repository.VpnRepository
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FetchServerListUseCase(private val repository: VpnRepository) {
    suspend fun execute(version: Int): Result<Response<ResponseBody>> {
        return try {
            val response = repository.fetchServerList(version)
            if (response.isSuccessful) {
                Result.success(response)
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Internet Error"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("connection timeout"))
        } catch (e: IOException) {
            Result.failure(Exception("Input output error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}