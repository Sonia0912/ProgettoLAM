package com.sonianicoletti.progettolam.di.module

import com.sonianicoletti.zxing.QRCodeGenerator
import com.sonianicoletti.zxing.ZxingQRCodeGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class QRCodeModule {

    @Binds
    abstract fun bindQRCodeGenerator(zxingQRCodeGenerator: ZxingQRCodeGenerator): QRCodeGenerator
}
