package com.example.pratilipiassignment.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.pratilipiassignment.databinding.BottomPopBlockBinding
import com.example.pratilipiassignment.model.Contact
import com.example.pratilipiassignment.utils.checkNumber
import com.example.pratilipiassignment.viewmodels.ContactViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class BlockContactFragment : BottomSheetDialogFragment() {

    private val contactViewModel by activityViewModels<ContactViewModel>()
    private lateinit var binding: BottomPopBlockBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomPopBlockBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            //launcher for choosing contacts from the contacts app on a phone
            val launcher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                when (it.resultCode) {

                    Activity.RESULT_OK -> {
                        contactPicked(it)?.let { pickedContact ->
                            contactViewModel.insert(pickedContact)
                            dismissAllowingStateLoss()
                        }
                    }

                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(
                            activity,
                            "Didn't choose a contact to block",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            buttonFromContacts.setOnClickListener { view ->
                val pickContactIntent =
                    Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

                launcher.launch(pickContactIntent)
            }

            editTextBlockNumber.addTextChangedListener {
                inputLayoutBlock.error = null
            }

            fabBlockBottom.setOnClickListener {
                if (editTextBlockNumber.checkNumber()) {
                    //TODO- Check if the number is already in contacts
                    contactViewModel.insert(
                        Contact(
                            "Unknown",
                            editTextBlockNumber.text.toString()
                        )
                    )
                    editTextBlockNumber.text = null
                } else {
                    inputLayoutBlock.error = "Contact Number is invalid"
                }
            }
        }
    }

    //We retrieve the picked contact from the Contacts app and return a Contact object
    private fun contactPicked(result: ActivityResult): Contact? {
        try {
            val uri: Uri = result.data?.data!!
            context?.contentResolver?.query(
                uri,
                null,
                null,
                null,
                null
            )?.use { contactCursor ->
                contactCursor.moveToFirst()
                val number =
                    contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val name =
                    contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                Log.d("BlockContactFragment", "Name = $name, number = $number")
                if(name != null && number != null){
                    return Contact(name = name,number = number)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(activity,e.toString(),Toast.LENGTH_SHORT).show()
        }
        return null
    }



}