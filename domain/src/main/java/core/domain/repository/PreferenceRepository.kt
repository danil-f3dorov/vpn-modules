package core.domain.repository

interface PreferenceRepository {
    fun getVersion(): Int
    fun setVersion(version: Int)
    fun resetVersion()
}