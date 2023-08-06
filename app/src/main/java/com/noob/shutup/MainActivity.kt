package com.noob.shutup

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noob.shutup.ui.theme.ShutupTheme

class MainActivity : ComponentActivity() {

    private val permissionsToRequest = arrayOf(
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        /*
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
         */
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            ShutupTheme {
                
                var btnLabel by remember { mutableStateOf("mute") }

                val viewModel = viewModel<MainViewModel>()

                val dialogQueue = viewModel.visiblePermissionDialogQueue

                val audioManager = LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                val notificationManager = LocalContext.current.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { perms ->
                        permissionsToRequest.forEach { permission ->
                            viewModel.onPermissionResult(
                                permission = permission,
                                isGranted = perms[permission] == true
                            )
                        }
                    }
                )

                val preMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val preNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {

                            // check permission
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                startActivity(intent)
                            }

                            multiplePermissionResultLauncher.launch(permissionsToRequest)

                            // toggle mute mode
                            if(audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE){
                                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preMusicVolume, 0)
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, preNotificationVolume, 0)
                                btnLabel = "mute"
                            }else{
                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                                // set media volume to zero
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                                // mute the notification and system sound
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
                                btnLabel = "restore"
                            }
                        },
                        modifier = Modifier.size(width = 140.dp, height = 50.dp)
                    ) {
                        Text(text = btnLabel, color = Color.White)
                    }
                }

                dialogQueue.reversed().forEach { permission ->
                    PermissionDialog(
                        permissionTextProvider = when (permission) {
                            Manifest.permission.MODIFY_AUDIO_SETTINGS-> {
                                AudioPermissionTextProvider()
                            }
                            Manifest.permission.CAMERA -> {
                                CameraPermissionTextProvider()
                            }
                            Manifest.permission.RECORD_AUDIO -> {
                                RecordAudioPermissionTextProvider()
                            }
                            Manifest.permission.CALL_PHONE -> {
                                PhoneCallPermissionTextProvider()
                            }
                            else -> return@forEach
                                                               },
                        isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                            permission
                        ),
                        onDismiss = viewModel::dismissDialog,
                        onOkClick = {
                            viewModel.dismissDialog()
                            multiplePermissionResultLauncher.launch(
                                arrayOf(permission)
                            )
                        },
                        onGoToAppSettingsClick = ::openAppSettings
                    )
                }
            }

        }

    }



}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}