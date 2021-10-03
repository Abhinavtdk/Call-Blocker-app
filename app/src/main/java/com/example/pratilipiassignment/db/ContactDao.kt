package com.example.pratilipiassignment.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pratilipiassignment.model.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg contact: Contact): List<Long>

    @Query("SELECT * FROM blockedContacts")
    suspend fun getAllBlockedContacts() : List<Contact>

    @Delete
    suspend fun delete(vararg contact: Contact)

    @Query("SELECT * FROM blockedContacts")
    fun getBlockedContactsLiveData() : LiveData<List<Contact>>

}