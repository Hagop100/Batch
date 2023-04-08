
package com.example.batchtest.FirebaseMessaging

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.batchtest.UserProfileTab.EditProfileFragment
import com.example.batchtest.LoginFragment
import com.example.batchtest.MainActivity
import com.example.batchtest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL


/**Firebase Messaging Service class must be created to send notifications to users using
 * Firebase
 * Class Will both implement Receiving Notifications and sending out notifications
 * */

class MyFirebaseMessagingService(): FirebaseMessagingService() {

    private var userToken: String = ""

    companion object
    {
        const val TAG: String = "Firebase Messaging"
        const val FCM_KEY_TITLE: String = "title"
        const val FCM_KEY_MESSAGE: String = "message"

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

            val title = message.data[FCM_KEY_TITLE]!!
            val messageReceived = message.data[FCM_KEY_MESSAGE]!!

            if(!isAppInForeground(this))
            {
                //sending the notification to the user with the incoming message
                sendNotification(title, messageReceived)
            }
            //sending the notification to the user with the incoming message

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
        userToken = token
        updateUserProfileToken(userToken)
    }

    fun updateUserProfileToken(token: String)
    {
        val currentUser = FirebaseAuth.getInstance().currentUser
        FirebaseFirestore.getInstance().collection("users").document(currentUser!!.uid)
            .update("userToken", token).addOnSuccessListener {
                Log.i(TAG, "new Token updated")
            }.addOnFailureListener{
                Log.i(TAG, "Failed to update token")
            }
    }




    /**Create a Notification and a notification channel  */
    private fun sendNotification(title: String , messageBody: String )
    {

        //TODO Check whether user is logged in.
        val intent = Intent(this, MainActivity::class.java)
        //Flags puts activity on the top of the stack
        //make sure the activities aren't overlapping each other and not have multiple of the same activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //must create a pending intent which is used because the user might be using another application
        //This will grant the right for the other application to open the intent with same permissions
        //Flag one shot - intent should only be used once
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val channeId = this.resources.getString(R.string.default_notification_channel_id)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        //must create a notification builder to build a notification
        //auto cancel makes sure to cancel notification once user clicks on it
        //TODO create vector for notification icon
        //TODO create TITLE and TEXT Body
        val notificationBuilder = NotificationCompat.Builder(
            this, channeId
        ).setSmallIcon(R.drawable.batch_icon)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        //Must create Notification Manager to manage notifications.
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Notification channel is needed for android 8 and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(channeId, "Message",
            NotificationManager.IMPORTANCE_DEFAULT)

            //Manager creates channel
            notificationManager.createNotificationChannel(channel)
        }

        //This finally builds the notification
        notificationManager.notify(0, notificationBuilder.build())
    }

    fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    processInfo.processName == context.packageName
                ) {
                    return true
                }
            }
        }
        return false
    }




}
