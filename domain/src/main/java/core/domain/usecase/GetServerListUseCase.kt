package core.domain.usecase

import core.domain.model.Server
import core.domain.repository.VpnRepository
import javax.inject.Inject

class GetServerListUseCase @Inject constructor(private val vpnRepository: VpnRepository) {
    suspend operator fun invoke(): List<Server> = vpnRepository.getServerList()
}