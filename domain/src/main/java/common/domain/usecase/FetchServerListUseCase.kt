package common.domain.usecase

import common.domain.repository.VpnRepository
import okhttp3.ResponseBody
import retrofit2.Response

class FetchServerListUseCase(private val repository: VpnRepository) {
    suspend fun execute(version: Int): Response<ResponseBody> {
        return repository.fetchServerList(version)
    }
}