package com.example.gpsdemo


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationServiceScreen()
        }
    }

    @Composable
    fun LocationServiceScreen() {
        Column {
            Button(onClick = {
                // フォアグラウンドサービス開始
                val startIntent = Intent(this@MainActivity, LocationService::class.java)
                startService(startIntent)
            }) {
                Text("Start Location Service")
            }

            Button(onClick = {
                // フォアグラウンドサービス停止
                val stopIntent = Intent(this@MainActivity, LocationService::class.java)
                stopService(stopIntent)
            }) {
                Text("Stop Location Service")
            }
        }
    }
}