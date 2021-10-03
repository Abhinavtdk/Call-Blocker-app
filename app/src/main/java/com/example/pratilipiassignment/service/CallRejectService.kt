package com.example.pratilipiassignment.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pratilipiassignment.MainActivity
import com.example.pratilipiassignment.R
import com.example.pratilipiassignment.db.ContactDao
import com.example.pratilipiassignment.model.Contact
import com.example.pratilipiassignment.utils.findBlockedContact
import com.example.pratilipiassignment.utils.mobileNumber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.N)
class CallRejectService : CallScreeningService(), CoroutineScope {

    @Inject
    lateinit var contactDao: ContactDao

    @Inject
    lateinit var notificationManagerCompat: NotificationManagerCompat

    override fun onScreenCall(details: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED
        ) {
//            notificationManagerCompat.sendNotificationForPermissions(this)
            //TODO - Ask for permissions if not granted
            return
        }


        // Skip all calls except Incoming
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && details.callDirection != Call.Details.DIRECTION_INCOMING
        ) return

        // parse incoming phone number from callDetails
        val incomingNumber = details.handle.mobileNumber() ?: return

        // fetch blocked numbers from db and match with incoming number
        launch {
            contactDao.getAllBlockedContacts().let { blockedList ->

                val contactToBeBlocked = blockedList.findBlockedContact(incomingNumber)
                if (contactToBeBlocked != null) {
                    endCall(contactToBeBlocked, details)
                }
            }
        }
    }

    private fun endCall(contact: Contact, details: Call.Details) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {

                val response = CallResponse.Builder()
                response.setRejectCall(true)
                response.setDisallowCall(true)
                response.setSkipCallLog(false)
                response.setSkipNotification(true)

                // request to reject call
                respondToCall(details, response.build())

                // send notification about the blocked call
//                notificationManagerCompat.sendNotification(this, contact)
                showNotification(this,contact)
            }
        }
    }

    private fun showNotification(context: Context, blockedNumber: Contact) {
        val intent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            0
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("block", "block", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, "block")
            .setSmallIcon(R.drawable.ic_block)
            .setContentTitle("Call blocked")
            .setContentText("Call blocked from ${blockedNumber.name} with the number ${blockedNumber.number}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationBuilder.setContentIntent(intent)
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND)
        notificationBuilder.setAutoCancel(true)
        notificationManager?.notify(1,notificationBuilder.build())
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d("CallReceiver", "CoroutineContext = $coroutineContext : Throwable = $throwable ")
        }
}