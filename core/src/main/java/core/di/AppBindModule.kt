package core.di

import core.data.repository.PreferenceRepositoryImpl
import core.data.repository.VpnRepositoryImpl
import core.domain.repository.PreferenceRepository
import core.domain.repository.VpnRepository
import dagger.Binds
import dagger.Module

@Module
interface AppBindModule {

    @Binds
    fun bindVpnRepositoryImpl_to_VpnRepository(vpnRepositoryImpl: VpnRepositoryImpl): VpnRepository

    @Binds
    @Suppress("Function_name")
    fun bindPreferenceRepositoryImpl_to_PreferenceRepository(preferenceRepositoryImpl: PreferenceRepositoryImpl): PreferenceRepository
}