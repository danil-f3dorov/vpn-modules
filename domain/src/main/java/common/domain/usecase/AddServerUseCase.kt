package common.domain.usecase

import common.domain.model.Server
import common.domain.repository.VpnRepository

class AddServerUseCase(private val vpnRepository: VpnRepository) {
    suspend fun execute(server: Server) {
        vpnRepository.addServer(server)
    }
}