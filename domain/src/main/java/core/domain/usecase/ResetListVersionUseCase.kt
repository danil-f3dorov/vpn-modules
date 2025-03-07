package core.domain.usecase

import core.domain.repository.PreferenceRepository
import javax.inject.Inject

class ResetListVersionUseCase @Inject constructor(private val preferenceRepository: PreferenceRepository) {
    operator fun invoke() {
        preferenceRepository.resetVersion()
    }
}