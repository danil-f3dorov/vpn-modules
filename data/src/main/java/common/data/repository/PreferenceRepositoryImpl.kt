package common.data.repository

import common.data.local.preferences.PrefStorage
import common.domain.repository.PreferenceRepository
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
