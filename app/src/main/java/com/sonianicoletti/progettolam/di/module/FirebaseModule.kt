package com.sonianicoletti.progettolam.di.module

import com.sonianicoletti.progettolam.FirebaseAuthService
import com.sonianicoletti.progettolam.FirebaseGameService
import com.sonianicoletti.progettolam.FirebaseMessagingTokenRefresher
import com.sonianicoletti.progettolam.FirebaseUserService
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import com.sonianicoletti.usecases.servives.UserService
import com.sonianicoletti.usecases.utils.MessagingTokenRefresher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    // crea un binding per AuthService usando FirebaseAuthService come un'implementazione
    @Binds
    abstract fun bindAuthService(firebaseAuthService: FirebaseAuthService): AuthService

    @Binds
    abstract fun bindGameService(firebaseGameService: FirebaseGameService): GameService

    @Binds
    abstract fun bindUserService(firebaseUserService: FirebaseUserService): UserService

    @Binds
    abstract fun bindMessagingTokenRefresher(firebaseMessagingTokenRefresher: FirebaseMessagingTokenRefresher): MessagingTokenRefresher
}
