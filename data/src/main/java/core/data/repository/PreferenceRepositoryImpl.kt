package core.data.repository

import core.data.local.preferences.PrefStorage
import core.domain.repository.PreferenceRepository
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(private val prefStorage: PrefStorage) :
    PreferenceRepository {
    override fun getVersion(): Int = prefStorage.getVersion()

    override fun setVersion(version: Int) {
        prefStorage.setVersion(version)
    }

    override fun resetVersion() {
        prefStorage.resetVersion()
    }
}
