package core.di

import androidx.lifecycle.ViewModel
import core.data.repository.PreferenceRepositoryImpl
import core.data.repository.VpnRepositoryImpl
import core.di.annotations.ViewModelKey
import core.domain.repository.PreferenceRepository
import core.domain.repository.VpnRepository
import core.viewmodel.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface AppBindModule {

    @Binds
    @[IntoMap ViewModelKey(MainViewModel::class)]
    fun provideMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    fun bindVpnRepositoryImpl_to_VpnRepository(vpnRepositoryImpl: VpnRepositoryImpl): VpnRepository

    @Binds
    @Suppress("Function_name")
    fun bindPreferenceRepositoryImpl_to_PreferenceRepository(preferenceRepositoryImpl: PreferenceRepositoryImpl): PreferenceRepository
}