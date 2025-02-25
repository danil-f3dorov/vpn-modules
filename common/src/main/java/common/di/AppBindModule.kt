package common.di

import common.data.repository.PreferenceRepositoryImpl
import common.data.repository.VpnRepositoryImpl
import common.domain.repository.PreferenceRepository
import common.domain.repository.VpnRepository
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