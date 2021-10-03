package com.example.pratilipiassignment.viewmodels

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratilipiassignment.db.ContactDao
import com.example.pratilipiassignment.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject internal constructor(
    private val contactDao: ContactDao
) : ViewModel(), LifecycleObserver {

    fun getBlockedContacts() = contactDao.getBlockedContactsLiveData()

    fun insert(contact: Contact) = viewModelScope.launch {
        contactDao.insert(contact)
    }

    fun delete(contact: Contact) = viewModelScope.launch {
        contactDao.delete(contact)
    }

}