package common.domain.usecase

import common.domain.model.Server
import common.domain.repository.VpnRepository

class GetServerListUseCase(private val vpnRepository: VpnRepository) {
    suspend fun execute(): List<Server> {
        return vpnRepository.getServerList()
    }
}