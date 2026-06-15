package com.example.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ParentControlApplication
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.Locale

class LocationSharingService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    companion object {
        const val CHANNEL_ID = "parent_control_location_channel"
        const val NOTIFICATION_ID = 48812
        private const val TAG = "LocationSharingService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LocationSharingService Created")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LocationSharingService Started")
        
        // Setup details
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Start collecting locations
        val repository = ParentControlApplication.instance.repository
        val activeUser = repository.currentUserState.value
        if (activeUser != null && activeUser.role == "child") {
            // Report child online
            serviceScope.launch {
                repository.updateOnlineStatus(activeUser.uid, true)
            }
            startLocationUpdates(activeUser.uid)
        } else {
            Log.w(TAG, "No child session authenticated. Skipping location collection.")
            stopSelf()
        }

        return START_STICKY
    }

    private fun startLocationUpdates(childUid: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions missing. Exiting service.")
            stopSelf()
            return
        }

        // Request 5-second interval high accuracy tracking to meet "5-10 seconds when moving"
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        var lastUploadedLocation: Location? = null
        var lastUploadTime = 0L

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Verify permission granted
                if (ActivityCompat.checkSelfPermission(this@LocationSharingService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Location Permission check failed at runtime.")
                    return
                }

                // Verify GPS enabled
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                if (!isGpsEnabled) {
                    Log.w(TAG, "Waiting for GPS signal... GPS Provider is disabled.")
                    return
                }

                val currentLoc: Location = locationResult.lastLocation ?: run {
                    Log.w(TAG, "Waiting for GPS signal... Fused location is null.")
                    return
                }

                // Verify speed or distance to decide moving vs stationary
                val speedMps = currentLoc.speed
                val distanceMoved = lastUploadedLocation?.distanceTo(currentLoc) ?: 0f
                val isMoving = speedMps > 0.3f || distanceMoved > 3.0f

                val timeElapsedMs = System.currentTimeMillis() - lastUploadTime
                val shouldUpload = when {
                    lastUploadedLocation == null -> true
                    isMoving && timeElapsedMs >= 5000 -> true      // Every 5-10 seconds when moving
                    !isMoving && timeElapsedMs >= 30000 -> true    // Every 30-60 seconds when stationary
                    else -> false
                }

                if (shouldUpload) {
                    lastUploadedLocation = currentLoc
                    lastUploadTime = System.currentTimeMillis()

                    Log.d(TAG, "Location Uploaded: lat=${currentLoc.latitude}, lng=${currentLoc.longitude}, accuracy=${currentLoc.accuracy}, speed=$speedMps, bearing=${currentLoc.bearing}")

                    // Fire async repository save
                    serviceScope.launch {
                        val address = getAddressFromLatLng(currentLoc.latitude, currentLoc.longitude)
                        val battery = getBatteryPercentage()
                        val repo = ParentControlApplication.instance.repository
                        
                        repo.updateLocation(
                            childUid = childUid,
                            latitude = currentLoc.latitude,
                            longitude = currentLoc.longitude,
                            accuracy = currentLoc.accuracy,
                            speed = speedMps * 3.6f, // Convert m/s to km/h for UI display
                            bearing = currentLoc.bearing,
                            battery = battery,
                            address = address
                        )
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            mainLooper
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun getBatteryPercentage(): Int {
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private suspend fun getAddressFromLatLng(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = android.location.Geocoder(applicationContext, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addressFlow = CompletableDeferred<String>()
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val text = addr.getAddressLine(0) ?: "${addr.locality ?: ""}, ${addr.adminArea ?: ""}"
                        addressFlow.complete(text)
                    } else {
                        addressFlow.complete("Simulated Area, Cyber City")
                    }
                }
                addressFlow.await()
            } else {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Simulated Area, Cyber City"
                } else {
                    "Simulated Area, Cyber City"
                }
            }
        } catch (e: Exception) {
            // Clean fallback coordinate reporting
            "Zone coordinates: [${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}]"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Parent Control Safety Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies child when their location sharing is transparently active."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Sharing Active")
            .setContentText("Your parent can view your location transparently.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        Log.d(TAG, "LocationSharingService Destroyed")
        stopLocationUpdates()
        
        // Report offline in sandbox
        val repository = ParentControlApplication.instance.repository
        val activeUser = repository.currentUserState.value
        if (activeUser != null && activeUser.role == "child") {
            serviceScope.launch {
                repository.updateOnlineStatus(activeUser.uid, false)
            }
        }

        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
