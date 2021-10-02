package com.example.pratilipiassignment.utils

import android.net.Uri
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pratilipiassignment.R
import com.example.pratilipiassignment.model.Contact
import java.net.URLDecoder

fun RecyclerView.divider() {
    addItemDecoration(
        DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        ).apply {
            ContextCompat.getDrawable(context, R.drawable.divider)?.let {drawable->
                setDrawable(drawable)
            }
        }
    )
}

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

fun Uri.mobileNumber() = runCatching {
    URLDecoder.decode(toString().replace("tel:",""),"UTF-8")
}.getOrNull()


