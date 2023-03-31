/*
package com.example.batchtest.FirebaseMessaging

import android.app.Activity
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
import androidx.core.app.NotificationCompat
import com.example.batchtest.UserProfileTab.EditProfileFragment
import com.example.batchtest.LoginFragment
import com.example.batchtest.MainActivity
import com.example.batchtest.R
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


*/
/**Firebase Messaging Service class must be created to send notifications to users using
 * Firebase*//*

class MyFirebaseMessagingService: FirebaseMessagingService() {


    companion object
    {
        var token = FirebaseMessaging.getInstance().token
        const val TAG: String = "Firebase Messaging"
        const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
        const val FCM_AUTHORIZATION: String = "authorization"
        const val FCM_KEY: String = "key"
        const val FCM_SERVER_KEY: String = "AAAAE3qZtkI:APA91bGvgqoL6w4h4qnYbjdM15tCaO9dcFhKG6k4gyLopezbWrVrxVbHEdzciFGyF8cVT9-evVHyudXM9oMQx7x9FFFE1_FJqhep7TatJh5rfoOT_aZfhwCUn09iplVno5mL6A9gKKlE"
        const val FCM_KEY_TITLE: String = "title"
        const val FCM_KEY_MESSAGE: String = "message"
        const val FCM_KEY_DATA: String = "data"
        const val FCM_KEY_TO: String = "to"
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

            //sending the notification to the user with the incoming message
            sendNotification(title, messageReceived)

        }

        //For Debugging
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    */
/**Automatically called when a new token is created or updated
     * Called when initially token is generated*//*

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        //Debugging
        Log.e(TAG, "New Token Created: $token")
        //update token in the user profile in the database
        updateUserProfileToken(token)
    }

    private fun updateUserProfileToken(token: String?)
    {
        //TODO update the userToken in the database
    }

    */
/**Create a Notification and a notification channel and stream to cloud *//*

    private fun sendNotification(title: String , messageBody: String )
    {
        //TODO Check whether user is logged in.
        val intent = Intent(this, LoginFragment::class.java)
        //Flags puts activity on the top of the stack
        //make sure the activities aren't overlapping each other and not have multiple of the same activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //must create a pending intent which is used because the user might be using another application
        //This will grant the right for the other application to open the intent with same permissions
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
            val channel = NotificationChannel(channeId, "Matching",
            NotificationManager.IMPORTANCE_DEFAULT)

            //Manager creates channel
            notificationManager.createNotificationChannel(channel)
        }

        //This finally builds the notification
        notificationManager.notify(0, notificationBuilder.build())
    }


    */
/**
     * Creates an object to output stream a notification
     **//*

    inner class SendNotificationToUserAsyncTask(val title: String, private val message: String, val token: String ): AsyncTask<Any, Void, String>(){

        */
/**
         * Creates an object to output stream a notification
         * @title title of notification
         * @message message of the notification
         * @token the user who will receive the token
         *
         * @return result result of the connection*//*

        override fun doInBackground(vararg p0: Any?): String {
            var result:String
            var connection: HttpURLConnection?= null
            try {
                //set the connection necessary to output the notification
                val url = URL(FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod= "POST"

                //These are the typical request settings
                connection.setRequestProperty("Content_Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")


                connection.setRequestProperty(
                    FCM_AUTHORIZATION, "${FCM_KEY}=${FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val outputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()

                //Create a Json with the notification data
                dataObject.put(FCM_KEY_TITLE, title)
                dataObject.put(FCM_KEY_MESSAGE, message)

                //Now we put the data into the request
                //and the token of the user we are sending to.
                jsonRequest.put(FCM_KEY_DATA,dataObject)
                jsonRequest.put(FCM_KEY_TO, token)

                outputStream.writeBytes(jsonRequest.toString())
                outputStream.flush()
                outputStream.close()

                //get the result of the http connection
                val httpResult: Int = connection.responseCode

                if(httpResult == HttpURLConnection.HTTP_OK)
                {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try
                    {
                        while(reader.readLine().also{line= it} != null)
                        {
                            stringBuilder.append(line+"\n")
                        }
                    }catch (e: IOException)
                    {
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e: IOException)
                        {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                }
                else{
                    result = connection.responseMessage
                }

            }catch (e: SocketTimeoutException)
            {
                result = "Connection Timeout"
            }catch (e: java.lang.Exception)
            {
                result = "Error: " + e.message
            }finally {
                //if a connection exists- close it
                connection?.disconnect()
            }

            return result
        }
        //TODO when calling this class, one must call execute()

    }
}*/
