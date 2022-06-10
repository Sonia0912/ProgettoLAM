package com.sonianicoletti.progettolam.di.module

import android.content.Context
import com.sonianicoletti.dependencies.android.NetworkServiceImpl
import com.sonianicoletti.usecases.servives.NetworkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AndroidModule {

    @Provides
    fun provideNetworkService(@ApplicationContext applicationContext: Context): NetworkService =
        NetworkServiceImpl(applicationContext)
}
