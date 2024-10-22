package common.di

import common.domain.repository.VpnRepository
import common.domain.usecase.AddServerListUseCase
import common.domain.usecase.AddServerUseCase
import common.domain.usecase.DeleteServerUseCase
import common.domain.usecase.FetchServerListUseCase
import common.domain.usecase.GetServerListUseCase
import org.koin.dsl.module


val domainModule = module {
    single { provideAddServerListUseCase(get()) }
    single { provideAddServerUseCase(get()) }
    single { provideDeleteServerListUseCase(get()) }
    single { provideFetchServerListUseCase(get()) }
    single { provideGetServerListUseCase(get()) }
}

fun provideAddServerListUseCase(vpnRepository: VpnRepository): AddServerListUseCase {
    return AddServerListUseCase(vpnRepository)
}

fun provideAddServerUseCase(vpnRepository: VpnRepository): AddServerUseCase {
    return AddServerUseCase(vpnRepository)
}

fun provideDeleteServerListUseCase(vpnRepository: VpnRepository): DeleteServerUseCase {
    return DeleteServerUseCase(vpnRepository)
}

fun provideFetchServerListUseCase(vpnRepository: VpnRepository): FetchServerListUseCase {
    return FetchServerListUseCase(vpnRepository)
}

fun provideGetServerListUseCase(vpnRepository: VpnRepository): GetServerListUseCase {
    return GetServerListUseCase(vpnRepository)
}