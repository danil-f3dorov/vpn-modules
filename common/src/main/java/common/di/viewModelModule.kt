package common.di

import common.viewmodel.FetchServerListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { FetchServerListViewModel(get(), get(), get(), get(), get()) }
}