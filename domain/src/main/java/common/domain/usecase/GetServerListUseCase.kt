package common.domain.usecase

import common.domain.model.Server
import common.domain.repository.VpnRepository
import javax.inject.Inject

class GetServerListUseCase @Inject constructor(private val vpnRepository: VpnRepository) {
    suspend operator fun invoke(): List<Server> = vpnRepository.getServerList()
}