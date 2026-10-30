package com.aviva.controlfarmacia.di

import com.aviva.controlfarmacia.data.media.LocalMedicationPhotoStore
import com.aviva.controlfarmacia.data.reminder.AlarmReminderScheduler
import com.aviva.controlfarmacia.data.repository.MedicationRepositoryImpl
import com.aviva.controlfarmacia.domain.media.MedicationPhotoStore
import com.aviva.controlfarmacia.domain.reminder.ReminderScheduler
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the domain interfaces to their data-layer implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    abstract fun bindMedicationPhotoStore(impl: LocalMedicationPhotoStore): MedicationPhotoStore

    @Binds
    abstract fun bindReminderScheduler(impl: AlarmReminderScheduler): ReminderScheduler
}
