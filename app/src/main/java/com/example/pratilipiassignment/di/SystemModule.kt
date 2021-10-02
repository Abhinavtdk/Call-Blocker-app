package com.example.pratilipiassignment.di

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SystemModule {

    @Singleton
    @Provides
    @RequiresApi(Build.VERSION_CODES.Q)
    fun provideRoleManager(@ApplicationContext context: Context) =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        }else{
            Any()
        }

    @Singleton
    @Provides
    @RequiresApi(Build.VERSION_CODES.Q)
    fun provideNotificationManager(@ApplicationContext context: Context) = NotificationManagerCompat.from(context)

}