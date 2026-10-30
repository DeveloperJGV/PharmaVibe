package com.aviva.controlfarmacia.di

import android.content.Context
import androidx.room.Room
import com.aviva.controlfarmacia.data.local.AppDatabase
import com.aviva.controlfarmacia.data.local.Migrations
import com.aviva.controlfarmacia.data.local.dao.IntakeDao
import com.aviva.controlfarmacia.data.local.dao.MedicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(*Migrations.ALL)
            // Versions 1 and 2 never left development; anything newer needs a real migration.
            .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2)
            .build()

    @Provides
    fun provideMedicationDao(database: AppDatabase): MedicationDao = database.medicationDao()

    @Provides
    fun provideIntakeDao(database: AppDatabase): IntakeDao = database.intakeDao()
}
