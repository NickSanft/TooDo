package com.divora.toodo

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    @Provides
    @Singleton
    fun providePrizeDao(appDatabase: AppDatabase): PrizeDao {
        return appDatabase.prizeDao()
    }

    @Provides
    @Singleton
    fun providePointLedgerDao(appDatabase: AppDatabase): PointLedgerDao {
        return appDatabase.pointLedgerDao()
    }
}
