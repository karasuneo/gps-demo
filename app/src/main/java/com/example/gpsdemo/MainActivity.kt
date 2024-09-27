package com.example.gpsdemo


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 定期的に位置情報を取得するためのリクエスト設定
        locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5秒ごとに更新
            fastestInterval = 2000 // 最短で2秒ごとに更新
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // 位置情報の更新を受け取るコールバック
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // 位置情報が更新された場合の処理
                    Log.d(
                        "LocationUpdate",
                        "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                    )
                }
            }
        }

        setContent {
            LocationScreen()
        }
    }

    @Composable
    fun LocationScreen() {
        var location by remember { mutableStateOf<Location?>(null) }
        var permissionGranted by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
            // 位置情報の更新を開始
            StartLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        Column {
            if (permissionGranted) {
                Button(onClick = {
                    coroutineScope.launch {
                        // 現在の位置情報を取得
                        getLastLocation { loc ->
                            location = loc
                        }
                    }
                }) {
                    Text("Get Current Location")
                }
                location?.let {
                    Text("Latitude: ${it.latitude}, Longitude: ${it.longitude}")
                }
            } else {
                Text("Location permission denied or not granted")
            }
        }
    }

    private fun getLastLocation(onLocationResult: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onLocationResult(null)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc: Location? ->
                onLocationResult(loc)
            }
            .addOnFailureListener { e ->
                Log.e("LocationError", "Error getting location", e)
                onLocationResult(null)
            }
    }

    private fun StartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onStop() {
        super.onStop()
        // アクティビティが停止されたら位置情報の更新を停止
        StopLocationUpdates()
    }

    private fun StopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
