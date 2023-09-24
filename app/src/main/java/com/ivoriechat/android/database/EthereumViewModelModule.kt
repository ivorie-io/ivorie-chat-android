package com.ivoriechat.android.database

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.metamask.androidsdk.ApplicationRepository
import io.metamask.androidsdk.EthereumViewModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EthereumViewModelModule {

    @Provides
    @Singleton
    fun provideEthereumViewModel(repository: ApplicationRepository): EthereumViewModel {
        return EthereumViewModel(repository)
    }
}