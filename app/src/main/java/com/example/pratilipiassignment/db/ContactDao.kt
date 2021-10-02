package com.example.pratilipiassignment.db

import androidx.room.*
import com.example.pratilipiassignment.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg contact: Contact): List<Long>

    @Query("SELECT * FROM blockedContacts")
    suspend fun getAllBlockedContacts() : List<Contact>

    @Delete
    suspend fun delete(vararg contact: Contact)

    @Query("SELECT * FROM blockedContacts")
    fun getBlockedContactsFlow() : Flow<List<Contact>>

}