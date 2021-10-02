package com.example.pratilipiassignment.viewmodels

//import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pratilipiassignment.db.ContactDao
import com.example.pratilipiassignment.model.Contact
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject internal constructor(
    private val contactDao: ContactDao
) : ViewModel(), LifecycleObserver {

    fun getBlockedContacts() = contactDao.getBlockedContactsFlow().asLiveData()

    fun insert(contact: Contact) = viewModelScope.launch {
        contactDao.insert(contact)
    }

    fun delete(contact: Contact) = viewModelScope.launch {
        contactDao.delete(contact)
    }

}