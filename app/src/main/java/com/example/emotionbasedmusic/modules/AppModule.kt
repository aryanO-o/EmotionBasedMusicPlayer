package com.example.emotionbasedmusic.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAuthInstance(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideDatabaseInstance(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Singleton
    @Provides
    fun provideStorageInstance(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}