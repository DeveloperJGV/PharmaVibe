package com.aviva.controlfarmacia.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Injected instead of calling `now()` directly so time-dependent logic is testable. */
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
