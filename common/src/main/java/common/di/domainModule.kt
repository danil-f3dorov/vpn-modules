package common.di

import common.domain.repository.VpnRepository
import common.domain.usecase.AddServerListUseCase
import common.domain.usecase.AddServerUseCase
import common.domain.usecase.DeleteServerUseCase
import common.domain.usecase.FetchServerListUseCase
import common.domain.usecase.GetServerListUseCase
import org.koin.dsl.module


val domainModule = module {
    single { providesAddServerListUseCase(get()) }
    single { providesAddServerUseCase(get()) }
    single { providesDeleteServerListUseCase(get()) }
    single { providesFetchServerListUseCase(get()) }
    single { providesGetServerListUseCase(get()) }
}

fun providesAddServerListUseCase(vpnRepository: VpnRepository): AddServerListUseCase {
    return AddServerListUseCase(vpnRepository)
}

fun providesAddServerUseCase(vpnRepository: VpnRepository): AddServerUseCase {
    return AddServerUseCase(vpnRepository)
}

fun providesDeleteServerListUseCase(vpnRepository: VpnRepository): DeleteServerUseCase {
    return DeleteServerUseCase(vpnRepository)
}

fun providesFetchServerListUseCase(vpnRepository: VpnRepository): FetchServerListUseCase {
    return FetchServerListUseCase(vpnRepository)
}

fun providesGetServerListUseCase(vpnRepository: VpnRepository): GetServerListUseCase {
    return GetServerListUseCase(vpnRepository)
}