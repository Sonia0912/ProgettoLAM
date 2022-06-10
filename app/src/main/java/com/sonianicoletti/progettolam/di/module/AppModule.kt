package com.sonianicoletti.progettolam.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideAppScope(): CoroutineScope = CoroutineScope(Job())
}
