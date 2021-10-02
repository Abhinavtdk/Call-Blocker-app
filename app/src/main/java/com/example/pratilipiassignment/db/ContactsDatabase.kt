package com.example.pratilipiassignment.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pratilipiassignment.model.Contact

@Database(
    entities =[Contact::class],
    version = 1,
    exportSchema = false
)
abstract class ContactsDatabase: RoomDatabase() {

    abstract fun getContactDao() : ContactDao

    companion object{
        fun getDatabase(context: Context) = Room.databaseBuilder(
            context,
            ContactsDatabase::class.java,
            "blocked_contacts.db"
        ).build()
    }

}