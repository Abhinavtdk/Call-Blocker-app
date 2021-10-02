package com.example.pratilipiassignment.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "blockedContacts"
)
data class Contact(
    val name: String? = null,
    @PrimaryKey
    val number: String,
    val timeStamp : Long= System.currentTimeMillis()
)
