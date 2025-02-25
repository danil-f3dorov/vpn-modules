package common.domain.usecase

import common.domain.repository.PreferenceRepository
import javax.inject.Inject

class ResetListVersionUseCase @Inject constructor(private val preferenceRepository: PreferenceRepository) {
    operator fun invoke() {
        preferenceRepository.resetVersion()
    }
}