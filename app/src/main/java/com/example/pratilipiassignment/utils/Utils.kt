package com.example.pratilipiassignment.utils

import android.net.Uri
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pratilipiassignment.R
import com.example.pratilipiassignment.model.Contact
import java.net.URLDecoder

fun List<Contact>.findBlockedContact(incoming: String) = firstOrNull(){contact->
    val incomingNumber = incoming.replace(" ","")
    val blockedContactNumber = contact.number.replace(" ","")

    incomingNumber.contains(blockedContactNumber) || blockedContactNumber.contains(incomingNumber)
}

fun EditText.checkNumber(): Boolean{
    val regexPattern: String = "^[+]?[0-9]{10,13}\$"
    return Regex(
        regexPattern
    ).matches(text.toString())
}



