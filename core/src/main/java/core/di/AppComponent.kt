package core.di

import android.content.Context
import core.domain.usecase.GetServerListUseCase
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Component(modules = [AppBindModule::class, DatabaseModule::class, NetworkModule::class])
@Singleton
interface AppComponent {

    val factory: MultiViewModelFactory

    fun inject(context: Context)

    fun getServerListUseCase(): GetServerListUseCase

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}