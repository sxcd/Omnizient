package com.shanenworks.omnizient.di

import android.content.Context
import com.shanenworks.omnizient.data.local.OmnizientDatabase
import com.shanenworks.omnizient.data.local.dao.DocumentDao
import com.shanenworks.omnizient.data.local.dao.IndexEntryDao
import com.shanenworks.omnizient.data.repository.DocumentRepository
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
    fun provideOmnizientDatabase(@ApplicationContext context: Context): OmnizientDatabase {
        return OmnizientDatabase.getDatabase(context)
    }

    @Provides
    fun provideDocumentDao(database: OmnizientDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideIndexEntryDao(database: OmnizientDatabase): IndexEntryDao {
        return database.indexEntryDao()
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        indexEntryDao: IndexEntryDao
    ): DocumentRepository {
        return DocumentRepository(documentDao, indexEntryDao)
    }
}
