package com.example.pratilipiassignment.di

import android.content.Context
import com.example.pratilipiassignment.db.ContactsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = ContactsDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideContactDao(contactsDatabase: ContactsDatabase) = contactsDatabase.getContactDao()

}