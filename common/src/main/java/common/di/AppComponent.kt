package common.di

import android.content.Context
import androidx.lifecycle.ViewModel
import common.di.annotations.ViewModelKey
import common.viewmodel.MainViewModel
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton


@Component(modules = [AppBindsModule::class, AppBindModule::class, DatabaseModule::class, NetworkModule::class])
@Singleton
interface AppComponent {

    val factory: MultiViewModelFactory

    fun inject(context: Context)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}

@Module
interface AppBindsModule {
    @Binds
    @[IntoMap ViewModelKey(MainViewModel::class)]
    fun provideMainViewModel(mainViewModel: MainViewModel): ViewModel
}