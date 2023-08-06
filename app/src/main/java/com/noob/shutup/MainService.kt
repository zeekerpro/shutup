package com.noob.shutup

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.content.getSystemService

class MainService : Service() {

    var preMusicVolume = 5
    var preSystemVolume = 5
    var preNotificationVolume  = 6

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val notificationManager = getSystemService(NotificationManager::class.java)

        // check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }

        val audioManager = getSystemService(AudioManager::class.java)

        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL){
            preMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            preNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            // enable incoming call vibration mode
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            // set media volume to zero
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_SHOW_UI)
            // mute the notification and system sound
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_SHOW_UI )

            println("muted")
            println(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            println(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION))
        }else{
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preMusicVolume, AudioManager.FLAG_SHOW_UI )
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, preSystemVolume, AudioManager.FLAG_SHOW_UI )
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, preNotificationVolume, AudioManager.FLAG_SHOW_UI )
            println("restored")
            println(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            println(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION))
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}