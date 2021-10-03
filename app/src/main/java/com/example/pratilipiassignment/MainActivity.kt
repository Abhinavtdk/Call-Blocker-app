package com.example.pratilipiassignment

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pratilipiassignment.adapters.BlockedContactsAdapter
import com.example.pratilipiassignment.databinding.ActivityMainBinding
import com.example.pratilipiassignment.fragments.BlockContactFragment
import com.example.pratilipiassignment.model.Contact
import com.example.pratilipiassignment.viewmodels.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PERMISSIONS_ID = 1
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var blockedContactsAdapter: BlockedContactsAdapter
    private val blockContactFragment by lazy { BlockContactFragment() }
    private val contactViewModel by viewModels<ContactViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            with(blockedContactsRv) {
                blockedContactsAdapter =
                    BlockedContactsAdapter(object : BlockedContactsAdapter.OnClickListener {
                        override fun OnClickUnBlock(contact: Contact) {
                            contactViewModel.delete(contact)
                        }
                    })
                adapter = blockedContactsAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this.context,
                        LinearLayoutManager.VERTICAL
                    )
                )
            }

            fabBlockContact.setOnClickListener {
                blockContactFragment.show(supportFragmentManager, "BlockContactFragment")
            }
        }

//        observeBlocked()
        if (checkAndRequestPermissions()) {
            observeBlocked()
        }
    }

    private fun observeBlocked() {
        contactViewModel.getBlockedContacts().observe(this, Observer { contacts ->
            blockedContactsAdapter.differ.submitList(contacts)

            with(binding) {
                nestedScrollView.fullScroll(View.FOCUS_UP)
                initialTv.isVisible = contacts.isEmpty()
            }
        })
    }

    private fun checkAndRequestPermissions(): Boolean {
        val readContactPermision =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        val readCallLogPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
        val readPhoneStatePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        val callPhonePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        val answerPhoneCallsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
        } else{}

        val permissionsNeeded: MutableList<String> = ArrayList()
        if (readContactPermision != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS)
        }
        if (readCallLogPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CALL_LOG)
        }
        if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (callPhonePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CALL_PHONE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (answerPhoneCallsPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ANSWER_PHONE_CALLS)
            }

        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_PERMISSIONS_ID
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS_ID -> {
                val permissions: MutableMap<String, Int> = HashMap()
                permissions[Manifest.permission.READ_CONTACTS] = PackageManager.PERMISSION_GRANTED
                permissions[Manifest.permission.READ_CALL_LOG] = PackageManager.PERMISSION_GRANTED
                permissions[Manifest.permission.READ_PHONE_STATE] = PackageManager.PERMISSION_GRANTED
                permissions[Manifest.permission.CALL_PHONE] = PackageManager.PERMISSION_GRANTED
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    permissions[Manifest.permission.ANSWER_PHONE_CALLS] =
                        PackageManager.PERMISSION_GRANTED
                }

                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < permissions.size) {
                        permissions[permissions[i].toString()] = grantResults[i]
                        i++
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && permissions[Manifest.permission.READ_CONTACTS] == PackageManager.PERMISSION_GRANTED && permissions[Manifest.permission.READ_CALL_LOG] == PackageManager.PERMISSION_GRANTED && permissions[Manifest.permission.READ_PHONE_STATE] == PackageManager.PERMISSION_GRANTED && permissions[Manifest.permission.CALL_PHONE] == PackageManager.PERMISSION_GRANTED  && permissions[Manifest.permission.ANSWER_PHONE_CALLS] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "All permissions granted")
                        observeBlocked()

                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && permissions[Manifest.permission.READ_CONTACTS] == PackageManager.PERMISSION_GRANTED  && permissions[Manifest.permission.READ_CALL_LOG] == PackageManager.PERMISSION_GRANTED && permissions[Manifest.permission.READ_PHONE_STATE] == PackageManager.PERMISSION_GRANTED && permissions[Manifest.permission.CALL_PHONE] == PackageManager.PERMISSION_GRANTED) {
                        observeBlocked()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_CONTACTS
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_PHONE_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CALL_PHONE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_CALL_LOG
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ANSWER_PHONE_CALLS
                                )
                            ) {
                                showDialogPermissions("Service Permissions are required",
                                    DialogInterface.OnClickListener { _, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE ->
                                                checkAndRequestPermissions()
                                        }
                                    })
                            } else {
                                goToSettings("Give the required permissions to use the app. Go to settings?")
                            }
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_CONTACTS
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_PHONE_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CALL_PHONE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_CALL_LOG
                                )
                            ) {
                                showDialogPermissions("Service Permissions are required",
                                    DialogInterface.OnClickListener { _, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE ->
                                                checkAndRequestPermissions()
                                        }
                                    })
                            } else {
                                goToSettings("Give the required permissions to use the app. Go to settings?")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDialogPermissions(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    private fun goToSettings(msg: String) {
        val dialog =
            AlertDialog.Builder(this)
        dialog.setMessage(msg)
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:com.example.pratilipiassignment")
                    )
                )
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ ->
                checkAndRequestPermissions()
            }
        dialog.show()
    }


}