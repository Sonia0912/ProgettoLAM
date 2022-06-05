package com.sonianicoletti.progettolam.di.module

import com.sonianicoletti.dataaccess.GameRepositoryImpl
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataAccessModule {

    @Binds
    abstract fun bindGameRepository(gameRepositoryImpl: GameRepositoryImpl): GameRepository
}