package com.noob.shutup

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noob.shutup.ui.theme.ShutupTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShutupTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RequestAudioPermission {
                        GreetingScreen()
                    }
                }
            }
        }
    }

    // Request permission
    @Composable
    fun RequestAudioPermission(content: @Composable () -> Unit) {

        val permissions = arrayOf(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        val permissionRequested = remember { mutableStateOf(false) }

        LaunchedEffect(key1 = true) {
            requestPermissions(permissions, Companion.MODIFY_AUDIO_REQUEST_CODE)
            permissionRequested.value = true
        }

        if (permissionRequested.value) {
            content()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MODIFY_AUDIO_REQUEST_CODE) {
            // Handle permission result
            val audioManager = LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
    }

    companion object {
        const val MODIFY_AUDIO_REQUEST_CODE = 1001
    }


}

@Composable
fun GreetingScreen() {


    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row( Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center ) {
            Button(
                onClick = {
                    println("clicked must in silence mode now")
                },
                modifier = Modifier.size(width = 140.dp, height = 50.dp)
            ) {
                Text("shutup", color = Color.White)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShutupTheme {
    }
}