package core.domain.usecase

import core.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetListVersionUseCase @Inject constructor(private val preferenceRepository: PreferenceRepository) {
    operator fun invoke (version: Int) = preferenceRepository.setVersion(version)
}