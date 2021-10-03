package com.example.pratilipiassignment.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.internal.telephony.ITelephony
import com.example.pratilipiassignment.MainActivity
import com.example.pratilipiassignment.R
import com.example.pratilipiassignment.db.ContactDao
import com.example.pratilipiassignment.model.Contact
import com.example.pratilipiassignment.utils.findBlockedContact
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class CallReceiver : BroadcastReceiver(), CoroutineScope {

    companion object {
        const val CONTACT_ID = "block"
    }

    @Inject
    lateinit var contactDao: ContactDao

    override fun onReceive(context: Context, intent: Intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
            ) {
                //TODO - Ask for permissions not given
                return
            }
        }

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED && intent.getStringExtra(
                TelephonyManager.EXTRA_STATE
            ) == TelephonyManager.EXTRA_STATE_RINGING
        ) {
            /*From https://developer.android.com/reference/android/telephony/TelephonyManager
            Extra key used with the ACTION_PHONE_STATE_CHANGED broadcast for a String containing the
            incoming or outgoing phone number.
            This extra is only populated for receivers of the ACTION_PHONE_STATE_CHANGED broadcast which
            have been granted the Manifest.permission.READ_CALL_LOG and Manifest.permission.READ_PHONE_STATE permissions.
            For incoming calls, the phone number is only guaranteed to be populated when the EXTRA_STATE changes from
            EXTRA_STATE_IDLE to EXTRA_STATE_RINGING. If the incoming caller is from an unknown number,
            the extra will be populated with an empty string. For outgoing calls, the phone number is only guaranteed
            to be populated when the EXTRA_STATE changes from EXTRA_STATE_IDLE to EXTRA_STATE_OFFHOOK
            So you receive the broadcast twice, one with phone number and another without. We only take care of the
            broadcast with the phone number.
             */
            if (!intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
                Log.d("CallReceiver", "No extra")
                return
            }

            //Getting the incoming number
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            launch {
                contactDao.getAllBlockedContacts().let { contacts ->
                    val matchNumber = contacts.findBlockedContact(number!!)
                    if (matchNumber != null) {
                        endCall(context, matchNumber)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun endCall(context: Context, matchNumber: Contact) {
        //In versions P and above, we don't need the aidl interface to end calls.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            try {
                telecomManager.endCall()
                showNotification(context, matchNumber)
            } catch (e: Exception) {
                Log.d("CallReceiver", "endCall: ${e.toString()}")
            }
        } else {
            try {
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val clazz = Class.forName(telephonyManager.javaClass.name)
                val method = clazz.getDeclaredMethod("getITelephony")
                method.isAccessible = true
                val telephonyService: ITelephony = method.invoke(telephonyManager) as ITelephony
                telephonyService.endCall()

                showNotification(context, matchNumber)
            } catch (e: Exception) {
                Log.d("CallReceiver", "endCall: ${e.toString()}")
            }
        }
    }

    //Shows the notification for the call receiver if a number is blocked and attempted to call
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
                NotificationChannel(CONTACT_ID, "block", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CONTACT_ID)
            .setSmallIcon(R.drawable.ic_block)
            .setContentTitle("Call blocked")
            .setContentText("Call blocked from ${blockedNumber.name} with the number ${blockedNumber.number}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationBuilder.setContentIntent(intent)
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND)
        notificationBuilder.setAutoCancel(true)
        notificationManager?.notify(1, notificationBuilder.build())
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d("CallReceiver", "CoroutineContext = $coroutineContext : Throwable = $throwable ")
        }
}