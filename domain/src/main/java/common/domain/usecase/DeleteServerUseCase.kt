package common.domain.usecase

import common.domain.model.Server
import common.domain.repository.VpnRepository

class DeleteServerUseCase(private val vpnRepository: VpnRepository) {
    suspend fun execute(server: Server) {
        vpnRepository.deleteServer(server)
    }
}