package common.domain.usecase

import common.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetListVersionUseCase @Inject constructor(private val preferenceRepository: PreferenceRepository) {
    operator fun invoke (version: Int) = preferenceRepository.setVersion(version)
}