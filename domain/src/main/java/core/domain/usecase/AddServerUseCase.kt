package core.domain.usecase

import core.domain.model.Server
import core.domain.repository.VpnRepository
import javax.inject.Inject

class AddServerUseCase @Inject constructor(private val vpnRepository: VpnRepository) {
    suspend operator fun invoke(server: Server) = vpnRepository.addServer(server)

}