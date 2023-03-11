package com.example.batchtest.FirebaseMessaging

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.batchtest.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**Firebase Messaging Service class must be created to send notifications to users using
 * Firebase*/
class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object
    {
        const val TAG: String = "Firebase Messaging"
    }
    //Called when a message is received while application is in the foreground
    //Deals with data message - Google Cloud Messaging
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        //For Debugging
        Log.d(TAG, "From: ${message.from}")

        //For Debugging
        message.data.isNotEmpty().let {
            Log.d(TAG, "Message Data Payload: ${message.data}")
        }

        //For Debugging
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    /**Automatically called when a new token is created or updated
     * Called when initially token is generated*/
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        //Debugging
        Log.e(TAG, "New Token Created: $token")
    }

    private fun updateUserProfileToken(token: String?)
    {
        //TODO update the userToken in the database
    }

    /**Create a Notification and a notification channel and stream to cloud */
    private fun sendNotification(messageBody: String, activityIntent: Activity )
    {
        val intent = Intent(this, activityIntent::class.java)
        //Flags puts activity on the top of the stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //must create a pending intent which is used because the user might be using another application
        //Flag one shot - intent should only be used once
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channeId = this.resources.getString(R.string.default_notification_channel_id)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        //must create a notification builder to build a notification
        //auto cancel makes sure to cancel notification once user clicks on it
        //TODO create vector for notification icon
        //TODO create TITLE and TEXT Body
        val notificationBuilder = NotificationCompat.Builder(
            this, channeId
        ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notification")
            .setContentText("Notification Text Message")
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        //Must create Notification Manager to manage notifications.
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Notification channel is needed for android 8 and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(channeId, "Matching",
            NotificationManager.IMPORTANCE_DEFAULT)

            //Manager creates channel
            notificationManager.createNotificationChannel(channel)
        }

        //This finally builds the notification
        notificationManager.notify(0, notificationBuilder.build())
    }
}