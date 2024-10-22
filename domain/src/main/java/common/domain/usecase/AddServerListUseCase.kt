package common.domain.usecase

import common.domain.model.Server
import common.domain.repository.VpnRepository

class AddServerListUseCase(private val vpnRepository: VpnRepository) {
    suspend fun execute(list: List<Server>) {
        vpnRepository.addSererList(list)
    }
}