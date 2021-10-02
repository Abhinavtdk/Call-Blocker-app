package com.example.pratilipiassignment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import com.example.pratilipiassignment.utils.divider
import com.example.pratilipiassignment.viewmodels.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
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
                divider()
            }

            fabBlockContact.setOnClickListener {
                blockContactFragment.show(supportFragmentManager, "BlockContactFragment")
            }
        }

//        observeBlocked()
        if(checkAndRequestPermissions()){
            observeBlocked()
        }
        //Permissions
//        when {
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
//                requestPermissions(
//                    arrayOf(
//                        android.Manifest.permission.ANSWER_PHONE_CALLS,
//                        android.Manifest.permission.READ_CONTACTS
//                    )
//                ) {}
//                requestRoleAsScreener()
//            }
//            else -> {
//                val permissions = arrayListOf(
//                    android.Manifest.permission.READ_CONTACTS,
//                    android.Manifest.permission.READ_CALL_LOG,
//                    android.Manifest.permission.READ_PHONE_STATE,
//                    android.Manifest.permission.CALL_PHONE
//                )
//                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
//                    permissions.add(android.Manifest.permission.ANSWER_PHONE_CALLS)
//                }
//                requestPermissions(permissions.toTypedArray()){
//                    observeBlocked()
//                }
//            }
//        }

    }

    private fun observeBlocked() {
        contactViewModel.getBlockedContacts().observe(this, Observer {contacts->
            blockedContactsAdapter.differ.submitList(contacts)

            with(binding){
                nestedScrollView.fullScroll(View.FOCUS_UP)
                initialTv.isVisible = contacts.isEmpty()
            }
        })
    }

//    private fun requestPermissions(permissions: Array<out String>, execute: () -> Unit) {
//        val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissionsGranted->
//            if(permissionsGranted.all { it.value }){
//                execute()
//            }else{
//
//            }
//        }
//    }

//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun requestRoleAsScreener() {
//        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){activityResult->
//            when(activityResult.resultCode){
//                Activity.RESULT_OK->{
//                    observeBlocked()
//                }
//                Activity.RESULT_CANCELED->{
//
//                }
//            }
//        }
//
//        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
//        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
//        launcher.launch(intent)
//    }






    private fun checkAndRequestPermissions(): Boolean {
        val readcontactpermision = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        val readphonepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        val callphonepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        val calllogpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
        val ansphonepermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
        } else {

        }


        val listPermissionsNeeded: MutableList<String> =
            ArrayList()
        if (readcontactpermision != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS)
        }
        if (readphonepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (callphonepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE)
        }
        if (calllogpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CALL_LOG)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ansphonepermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ANSWER_PHONE_CALLS)
            }

        }


        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                val perms: MutableMap<String, Int> =
                    HashMap()
                // Initialize the map with all the permissions
                perms[Manifest.permission.READ_CONTACTS] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_PHONE_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.CALL_PHONE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_CALL_LOG] = PackageManager.PERMISSION_GRANTED
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    perms[Manifest.permission.ANSWER_PHONE_CALLS] = PackageManager.PERMISSION_GRANTED
                }


                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }
                    // Check for all permissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && perms[Manifest.permission.READ_CONTACTS] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.READ_PHONE_STATE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.CALL_PHONE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.READ_CALL_LOG] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.ANSWER_PHONE_CALLS] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "All permissions granted")
                        observeBlocked()

                        //else any one or all the permissions are not granted
                    } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O && perms[Manifest.permission.READ_CONTACTS] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.READ_PHONE_STATE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.CALL_PHONE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.READ_CALL_LOG] == PackageManager.PERMISSION_GRANTED
                    ) {
                        observeBlocked()
                    }
                    else {
                        // For OS versions greater than andorid 8.0
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d(TAG, "Some permissions are not granted ask again ")
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                            // shouldShowRequestPermissionRationale will return true
                            //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
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
                                showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { _, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE ->
                                                checkAndRequestPermissions()  // proceed with logic by disabling the related features or quit the app.
                                        }
                                    })
                            } else {
                                explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                                //proceed with logic by disabling the related features or quit the app.
                            }
                        } else{
                            // For OS versions less than andorid 8.0
                            Log.d(TAG, "Some permissions are not granted ask again ")
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                            // shouldShowRequestPermissionRationale will return true
                            //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
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
                                showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { _, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE ->
                                                checkAndRequestPermissions()  // proceed with logic by disabling the related features or quit the app.
                                        }
                                    })
                            } else {
                                explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                                //proceed with logic by disabling the related features or quit the app.
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDialogOK(
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

    private fun explain(msg: String) {
        val dialog =
            AlertDialog.Builder(this)
        dialog.setMessage(msg)
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:com.vrihas.assignment.pratilipi.pratilipiapp")
                    )
                )
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ ->
                checkAndRequestPermissions()
                // finish();
            }
        dialog.show()
    }



}