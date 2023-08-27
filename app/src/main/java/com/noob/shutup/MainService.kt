package com.noob.shutup

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings

class MainService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val sharedPreferences = this.getSharedPreferences("shutup", Context.MODE_PRIVATE)
        var preMusicVolume = sharedPreferences.getInt("preMusicVolume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        var preRingVolume = sharedPreferences.getInt("preRingVolume", audioManager.getStreamVolume(AudioManager.STREAM_RING))
        var preNotificationVolume  = sharedPreferences.getInt("preNotificationVolume", audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION))

        val notificationManager = getSystemService(NotificationManager::class.java)

        val editor = sharedPreferences.edit()

        // check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }

        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL){
            preMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            preRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            preNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

            // store current volume of system
            editor.putInt("preMusicVolume", preMusicVolume)
            editor.putInt("preRingVolume", preRingVolume)
            editor.putInt("preNotificationVolume", preNotificationVolume)
            editor.apply()

            // enable incoming call vibration mode
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            // set media volume to zero
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)

            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_SHOW_UI)
            // mute the notification and system sound
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_SHOW_UI )

            // test
            while (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            }

            println("muted")
            println(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            println(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION))
        }else{
            editor.putInt("preMusicVolume", preMusicVolume)
            editor.putInt("preRingVolume", preRingVolume)
            editor.putInt("preNotificationVolume", preNotificationVolume)

            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preMusicVolume, AudioManager.FLAG_SHOW_UI )
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, preRingVolume, AudioManager.FLAG_SHOW_UI )
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, preNotificationVolume, AudioManager.FLAG_SHOW_UI )

            // test
            while (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < preMusicVolume) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
            }

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